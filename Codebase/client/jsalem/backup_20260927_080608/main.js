const { app, BrowserWindow, ipcMain, dialog, shell, Menu } = require('electron');
const path = require('path');
const fs = require('fs');
const http = require('http');
const https = require('https');
const sharp = require('sharp');

let mainWindow;
let tileServer = null;
let currentSource = null; // { type: 'local'|'github', path?, repo?, token? }
let livePosition = null;
let tileCache = {};

// Config file for saved repos/settings
const configPath = path.join(app.getPath('userData'), 'config.json');

function loadConfig() {
    try {
        if (fs.existsSync(configPath)) {
            const cfg = JSON.parse(fs.readFileSync(configPath, 'utf8'));
            // Migrate old format
            if (!cfg.sources) cfg.sources = [];
            if (cfg.repos) {
                cfg.repos.forEach(r => {
                    if (!cfg.sources.find(s => s.type === 'github' && s.repo === r.repo)) {
                        // Parse repo from URL if needed
                        let repoName = r.repo;
                        let tilesPath = r.tilesPath || '';
                        const urlMatch = repoName.match(/github\.com\/([^/]+)\/([^/]+)/);
                        if (urlMatch) {
                            repoName = urlMatch[1] + '/' + urlMatch[2];
                        }
                        if (!tilesPath) {
                            const pathMatch = r.repo.match(/\/tree\/[^/]+\/?(.*)/);
                            if (pathMatch) tilesPath = pathMatch[1].replace(/\/+$/, '');
                        }
                        cfg.sources.push({ id: Date.now().toString(36), type: 'github', name: repoName, repo: repoName, tilesPath: tilesPath, token: cfg.githubToken || '' });
                    }
                });
                delete cfg.repos;
            }
            // Fix existing sources with bad repo format
            cfg.sources.forEach(s => {
                if (s.type === 'github' && s.repo && s.repo.includes('github.com')) {
                    const m = s.repo.match(/github\.com\/([^/]+)\/([^/]+)/);
                    if (m) s.repo = m[1] + '/' + m[2];
                }
                if (s.type === 'github' && !s.tilesPath) {
                    // Try to extract from name if it's a URL
                    const pathMatch = s.name.match(/\/tree\/[^/]+\/?(.*)/);
                    if (pathMatch) s.tilesPath = pathMatch[1].replace(/\/+$/, '');
                }
            });
            if (cfg.localFolder) {
                if (!cfg.sources.find(s => s.type === 'local' && s.path === cfg.localFolder)) {
                    cfg.sources.push({ id: Date.now().toString(36) + 'l', type: 'local', name: cfg.localFolder.split(/[\\/]/).pop(), path: cfg.localFolder });
                }
                delete cfg.localFolder;
            }
            return cfg;
        }
    } catch (e) {}
    return { sources: [], lastSourceId: null };
}

function saveConfig(config) {
    try {
        fs.writeFileSync(configPath, JSON.stringify(config, null, 2), 'utf8');
    } catch (e) {}
}

// ========== MARKERS ==========
function newMarkerId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
}

// Reads markers.json and gives any legacy marker a stable id, persisting the file once
// so game-side Save/Delete can address every marker.
function readMarkersFile(markerPath) {
    let markers = [];
    if (fs.existsSync(markerPath)) {
        try { markers = JSON.parse(fs.readFileSync(markerPath, 'utf8')); }
        catch (e) { markers = []; }
    }
    if (!Array.isArray(markers)) return [];
    let changed = false;
    const used = new Set();
    for (const m of markers) {
        if (m.id !== undefined && m.id !== null && m.id !== '') used.add(String(m.id));
    }
    for (const m of markers) {
        if (m.id === undefined || m.id === null || m.id === '') {
            let id;
            do { id = 'm' + newMarkerId(); } while (used.has(id));
            used.add(id);
            m.id = id;
            changed = true;
        }
    }
    if (changed) {
        try { fs.writeFileSync(markerPath, JSON.stringify(markers, null, 2), 'utf8'); }
        catch (e) {}
        console.log('[MARKERS] Normalized legacy ids in ' + markerPath);
    }
    return markers;
}

// ========== TILE SERVER ==========
function startTileServer(port = 18321) {
    if (tileServer) tileServer.close();

    tileServer = http.createServer(async (req, res) => {
        const url = new URL(req.url, `http://localhost:${port}`);

        if (url.pathname === '/tile') {
            const stage = url.searchParams.get('stage');
            const x = url.searchParams.get('x');
            const y = url.searchParams.get('y');
            const ext = url.searchParams.get('ext') || 'webp';

            if (!stage || !x || !y) {
                res.writeHead(400);
                res.end('Missing params');
                return;
            }

            const tileKey = `${stage}/tile_${x}_${y}.${ext}`;

            if (!currentSource) {
                res.writeHead(404);
                res.end('No source selected');
                return;
            }

            // Domain source - proxy to external URL
            if (currentSource.type === 'domain') {
                const baseUrl = currentSource.url.replace(/\/+$/, '');
                const tileUrl = `${baseUrl}/${tileKey}`;
                try {
                    const data = await fetchUrl(tileUrl);
                    if (data) {
                        res.writeHead(200, { 'Content-Type': getMime(ext), 'Cache-Control': 'public, max-age=300' });
                        res.end(data);
                        return;
                    }
                } catch (e) {
                    console.error('[TILE] Domain fetch error:', e.message);
                }
                res.writeHead(404);
                res.end('Tile not found on domain');
                return;
            }

            // GitHub source
            if (currentSource.type === 'github') {
                try {
                    const data = await fetchGitHubTile(currentSource, tileKey);
                    if (data && data.length > 100) {
                        res.writeHead(200, { 'Content-Type': getMime(ext), 'Cache-Control': 'public, max-age=300' });
                        res.end(data);
                        return;
                    }
                } catch (e) {
                    console.error('[TILE] GitHub fetch error:', e.message);
                }
                res.writeHead(404);
                res.end('Tile not found on GitHub');
                return;
            }

            // Local source
            if (currentSource.type === 'local') {
                let localPath = path.join(currentSource.path, 'tiles', tileKey);
                if (!fs.existsSync(localPath)) {
                    localPath = path.join(currentSource.path, tileKey);
                }
                try {
                    const data = fs.readFileSync(localPath);
                    res.writeHead(200, { 'Content-Type': getMime(ext), 'Cache-Control': 'public, max-age=86400' });
                    res.end(data);
                    return;
                } catch (e) {}
            }

            res.writeHead(404);
            res.end('Tile not found');
            return;
        }

        // Live position tracking
        if (url.pathname === '/position') {
            if (req.method === 'POST') {
                let body = '';
                req.on('data', chunk => body += chunk);
                req.on('end', () => {
                    try {
                        livePosition = JSON.parse(body);
                        livePosition.timestamp = Date.now();
                        res.writeHead(200, { 'Content-Type': 'application/json' });
                        res.end('{"ok":true}');
                    } catch (e) {
                        res.writeHead(400);
                        res.end('{"error":"Invalid JSON"}');
                    }
                });
                return;
            } else {
                // GET - return current position
                res.writeHead(200, { 'Content-Type': 'application/json', 'Cache-Control': 'no-cache' });
                res.end(JSON.stringify(livePosition || { x: 0, y: 0, name: '', timestamp: 0 }));
                return;
            }
        }

        // Marker sync endpoints
        if (url.pathname === '/markers') {
            if (req.method === 'GET') {
                try {
                    // parseFloat, not parseInt: the client sends a decimal span
                    // (e.g. x_max=22.999) and parseInt would truncate it to 22,
                    // dropping the trailing .5 of the last cell.
                    const xMin = parseFloat(url.searchParams.get('x_min'));
                    const xMax = parseFloat(url.searchParams.get('x_max'));
                    const yMin = parseFloat(url.searchParams.get('y_min'));
                    const yMax = parseFloat(url.searchParams.get('y_max'));
                    const xLo = Number.isFinite(xMin) ? xMin : -99999;
                    const xHi = Number.isFinite(xMax) ? xMax : 99999;
                    const yLo = Number.isFinite(yMin) ? yMin : -99999;
                    const yHi = Number.isFinite(yMax) ? yMax : 99999;
                    let markers = [];
                    if (currentSource && currentSource.type === 'local') {
                        const markerPath = currentSource.path + '/markers.json';
                        markers = readMarkersFile(markerPath);
                    }
                    const filtered = markers.filter(m =>
                        m.x >= xLo && m.x <= xHi && m.y >= yLo && m.y <= yHi
                    );
                    console.log('[MARKERS GET] Filter: x=' + xLo + '-' + xHi + ', y=' + yLo + '-' + yHi + ' | Total: ' + markers.length + ' | Matched: ' + filtered.length);
                    res.writeHead(200, { 'Content-Type': 'application/json', 'Cache-Control': 'no-cache' });
                    res.end(JSON.stringify(filtered));
                } catch (e) {
                    res.writeHead(500);
                    res.end('[]');
                }
                return;
            }
            if (req.method === 'POST') {
                let body = '';
                req.on('data', chunk => body += chunk);
                req.on('end', () => {
                    try {
                        const data = JSON.parse(body);
                        console.log('[MARKERS POST] Action: ' + data.action + ', Title: ' + data.title + ', x: ' + data.x + ', y: ' + data.y);
                        if (!currentSource || currentSource.type !== 'local') {
                            res.writeHead(400);
                            res.end('{"error":"No local source"}');
                            return;
                        }
                        const markerPath = currentSource.path + '/markers.json';
                        let markers = readMarkersFile(markerPath);
                        if (data.action === 'add') {
                            const marker = {
                                id: newMarkerId(),
                                title: data.title || 'Marker',
                                x: data.x || 0,
                                y: data.y || 0,
                                type: data.type || 'interest'
                            };
                            markers.push(marker);
                            fs.writeFileSync(markerPath, JSON.stringify(markers, null, 2));
                            if (mainWindow) mainWindow.webContents.send('markers-changed');
                            res.writeHead(200, { 'Content-Type': 'application/json' });
                            res.end(JSON.stringify({ ok: true, id: marker.id }));
                        } else if (data.action === 'update') {
                            const idx = markers.findIndex(m => m.id === data.id);
                            if (idx >= 0) {
                                if (data.x !== undefined) markers[idx].x = data.x;
                                if (data.y !== undefined) markers[idx].y = data.y;
                                if (data.title !== undefined) markers[idx].title = data.title;
                                if (data.type !== undefined) markers[idx].type = data.type;
                                fs.writeFileSync(markerPath, JSON.stringify(markers, null, 2));
                                if (mainWindow) mainWindow.webContents.send('markers-changed');
                                res.writeHead(200, { 'Content-Type': 'application/json' });
                                res.end('{"ok":true}');
                            } else {
                                res.writeHead(404);
                                res.end('{"error":"Not found"}');
                            }
                        } else if (data.action === 'delete') {
                            const idx = markers.findIndex(m => m.id === data.id);
                            if (idx >= 0) {
                                markers.splice(idx, 1);
                                fs.writeFileSync(markerPath, JSON.stringify(markers, null, 2));
                                if (mainWindow) mainWindow.webContents.send('markers-changed');
                                res.writeHead(200, { 'Content-Type': 'application/json' });
                                res.end('{"ok":true}');
                            } else {
                                res.writeHead(404);
                                res.end('{"error":"Not found"}');
                            }
                        } else {
                            res.writeHead(400);
                            res.end('{"error":"Unknown action"}');
                        }
                    } catch (e) {
                        res.writeHead(400);
                        res.end('{"error":"Invalid JSON"}');
                    }
                });
                return;
            }
        }

        // Serve static files (for ui.json images)
        if (url.pathname === '/file') {
            const filePath = url.searchParams.get('path');
            if (filePath && fs.existsSync(filePath)) {
                try {
                    const data = fs.readFileSync(filePath);
                    const ext = path.extname(filePath).toLowerCase();
                    const mimeTypes = { '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.gif': 'image/gif', '.webp': 'image/webp', '.svg': 'image/svg+xml' };
                    res.writeHead(200, { 'Content-Type': mimeTypes[ext] || 'application/octet-stream', 'Cache-Control': 'public, max-age=86400' });
                    res.end(data);
                    return;
                } catch (e) {}
            }
            res.writeHead(404);
            res.end('File not found');
            return;
        }

        res.writeHead(404);
        res.end('Unknown route');
        return;
    });

    tileServer.listen(port, '127.0.0.1', () => {
        console.log(`Tile server on http://127.0.0.1:${port}`);
    });

    tileServer.on('error', (e) => {
        if (e.code === 'EADDRINUSE') startTileServer(port + 1);
    });
}

function fetchUrl(url) {
    return new Promise((resolve, reject) => {
        const client = url.startsWith('https') ? https : http;
        const req = client.get(url, { headers: { 'User-Agent': 'WitchWatchers-Map' } }, (res) => {
            if (res.statusCode === 301 || res.statusCode === 302) {
                fetchUrl(res.headers.location).then(resolve).catch(reject);
                return;
            }
            const chunks = [];
            res.on('data', chunk => chunks.push(chunk));
            res.on('end', () => resolve(Buffer.concat(chunks)));
        });
        req.on('error', reject);
        req.setTimeout(10000, () => { req.destroy(); reject(new Error('Timeout')); });
    });
}

function getMime(ext) {
    return { webp: 'image/webp', png: 'image/png', jpg: 'image/jpeg' }[ext] || 'application/octet-stream';
}

// ========== GITHUB API ==========
function githubApi(url, token) {
    return new Promise((resolve, reject) => {
        const headers = {
            'Accept': 'application/vnd.github.v3+json',
            'User-Agent': 'WitchWatchers-Map'
        };
        if (token) headers['Authorization'] = `token ${token}`;
        const req = https.get(url, { headers }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                if (res.statusCode === 200) {
                    resolve(JSON.parse(data));
                } else if (res.statusCode === 404) {
                    resolve(null);
                } else {
                    reject(new Error(`GitHub API ${res.statusCode}: ${data}`));
                }
            });
        });
        req.on('error', reject);
    });
}

function fetchGitHubFile(token, owner, repo, filePath) {
    return new Promise((resolve, reject) => {
        const url = `https://api.github.com/repos/${owner}/${repo}/contents/${filePath}`;
        const headers = {
            'Accept': 'application/vnd.github.v3.raw',
            'User-Agent': 'WitchWatchers-Map'
        };
        if (token) headers['Authorization'] = `token ${token}`;
        const req = https.get(url, { headers }, (res) => {
            if (res.statusCode === 302 || res.statusCode === 301) {
                https.get(res.headers.location, (res2) => {
                    const chunks = [];
                    res2.on('data', chunk => chunks.push(chunk));
                    res2.on('end', () => resolve(Buffer.concat(chunks)));
                }).on('error', reject);
                return;
            }
            if (res.statusCode !== 200) {
                let body = '';
                res.on('data', chunk => body += chunk);
                res.on('end', () => reject(new Error(`GitHub ${res.statusCode}: ${body.substring(0, 200)}`)));
                return;
            }
            const chunks = [];
            res.on('data', chunk => chunks.push(chunk));
            res.on('end', () => resolve(Buffer.concat(chunks)));
        });
        req.on('error', reject);
        req.setTimeout(10000, () => { req.destroy(); reject(new Error('Timeout')); });
    });
}

async function fetchGitHubTile(source, tileKey) {
    const [owner, repo] = source.repo.split('/');
    const tilesPath = source.tilesPath || '';
    const fullPath = tilesPath ? `${tilesPath}/${tileKey}` : `tiles/${tileKey}`;
    // Use source token, or fall back to global token from config
    let token = source.token || '';
    if (!token) {
        try {
            const cfg = loadConfig();
            token = cfg.githubToken || '';
        } catch (e) {}
    }
    try {
        return await fetchGitHubFile(token, owner, repo, fullPath);
    } catch (e) {
        return null;
    }
}

async function checkGitHubAccess(token, repoSlug) {
    const [owner, repo] = repoSlug.split('/');
    try {
        const result = await githubApi(`https://api.github.com/repos/${owner}/${repo}`, token);
        return result !== null && result.private !== undefined;
    } catch (e) {
        return false;
    }
}

async function listGitHubTiles(token, repoSlug, dir = 'tiles') {
    const [owner, repo] = repoSlug.split('/');
    try {
        const items = await githubApi(`https://api.github.com/repos/${owner}/${repo}/contents/${dir}`, token);
        if (!items || !Array.isArray(items)) return [];
        return items.map(i => ({ name: i.name, type: i.type, path: i.path }));
    } catch (e) {
        return [];
    }
}

// ========== TILE CACHE ==========
function getCacheDir() {
    const dir = path.join(app.getPath('userData'), 'tile-cache');
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    return dir;
}

async function getCachedTile(key) {
    const safeKey = key.replace(/[\/\\]/g, '_');
    const cachePath = path.join(getCacheDir(), safeKey);
    try {
        if (fs.existsSync(cachePath)) {
            return fs.readFileSync(cachePath);
        }
    } catch (e) {}
    return null;
}

async function cacheTile(key, data) {
    const safeKey = key.replace(/[\/\\]/g, '_');
    const cachePath = path.join(getCacheDir(), safeKey);
    try {
        fs.writeFileSync(cachePath, data);
    } catch (e) {}
}

function getCacheSize() {
    const dir = getCacheDir();
    let size = 0;
    try {
        const files = fs.readdirSync(dir);
        files.forEach(f => {
            const stat = fs.statSync(path.join(dir, f));
            size += stat.size;
        });
    } catch (e) {}
    return size;
}

function clearCache() {
    const dir = getCacheDir();
    try {
        const files = fs.readdirSync(dir);
        files.forEach(f => fs.unlinkSync(path.join(dir, f)));
    } catch (e) {}
}

// ========== WINDOW ==========
function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1400,
        height: 900,
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            contextIsolation: true,
            nodeIntegration: false
        },
        icon: path.join(__dirname, 'icon.png'),
        title: 'WitchWatchers Map',
        autoHideMenuBar: true
    });

    mainWindow.loadFile(path.join(__dirname, 'index.html'));
    startTileServer();
}

// ========== IPC ==========
ipcMain.handle('load-config', () => loadConfig());
ipcMain.handle('save-config', (e, config) => saveConfig(config));

ipcMain.handle('select-local-folder', async () => {
    const result = await dialog.showOpenDialog(mainWindow, {
        properties: ['openDirectory'],
        title: 'Select Map Folder'
    });
    if (!result.canceled && result.filePaths.length > 0) {
        const folder = result.filePaths[0];
        const hasTiles = fs.existsSync(path.join(folder, 'tiles'));
        return { success: true, path: folder, hasTiles };
    }
    return { success: false };
});

ipcMain.handle('set-source', (e, source) => {
    currentSource = source;
    return { ok: true, type: source?.type };
});

ipcMain.handle('check-github-access', async (e, token, repo) => {
    return await checkGitHubAccess(token, repo);
});

ipcMain.handle('list-github-tiles', async (e, token, repo, dir) => {
    return await listGitHubTiles(token, repo, dir);
});

ipcMain.handle('github-repo-contents', async (e, token, repo, dir) => {
    const [owner, repoName] = repo.split('/');
    try {
        const items = await githubApi(`https://api.github.com/repos/${owner}/${repoName}/contents/${dir || ''}`, token);
        if (!items || !Array.isArray(items)) return [];
        return items.map(i => ({ name: i.name, type: i.type, path: i.path }));
    } catch (e) {
        return [];
    }
});

ipcMain.handle('download-github-tiles', async (e, token, repo) => {
    const [owner, repoName] = repo.split('/');
    const destFolder = getCacheDir();
    const allTiles = [];

    async function listDir(dir) {
        const items = await githubApi(`https://api.github.com/repos/${owner}/${repoName}/contents/${dir}`, token);
        if (!items || !Array.isArray(items)) return;
        for (const item of items) {
            if (item.type === 'dir') {
                await listDir(item.path);
            } else if (item.type === 'file' && /tile_-?\d+_-?\d+\.\w+/.test(item.name)) {
                allTiles.push(item.path);
            }
        }
    }

    await listDir('tiles');

    let downloaded = 0;
    const total = allTiles.length;

    for (const tilePath of allTiles) {
        try {
            const data = await fetchGitHubFile(token, owner, repoName, tilePath);
            if (data) {
                const localPath = path.join(destFolder, tilePath);
                const dir = path.dirname(localPath);
                if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
                fs.writeFileSync(localPath, data);
                downloaded++;
                mainWindow.webContents.send('download-progress', { downloaded, total, file: tilePath });
            }
        } catch (e) {
            console.error(`Failed to download ${tilePath}:`, e.message);
        }
    }

    return { success: true, downloaded, total };
});

ipcMain.handle('get-cache-size', () => getCacheSize());
ipcMain.handle('clear-cache', () => clearCache());
ipcMain.handle('fetch-url', async (e, url) => {
    try {
        const data = await fetchUrl(url);
        if (data) return data.toString('utf8');
    } catch (e) {}
    return null;
});

ipcMain.handle('check-domain-access', async (e, url) => {
    try {
        const cleanUrl = url.replace(/\/+$/, '');
        // Try to fetch a test tile
        const testUrl = `${cleanUrl}/0/tile_0_0.webp`;
        const data = await fetchUrl(testUrl);
        if (data && data.length > 1000) {
            return { ok: true, size: data.length };
        }
        // Check if it returned HTML (anti-bot page)
        const text = data ? data.toString('utf8').substring(0, 200) : '';
        if (text.includes('<html') || text.includes('<script') || text.includes('aes.js')) {
            return { ok: false, error: 'Blocked by anti-bot protection' };
        }
        return { ok: false, error: 'No valid tiles found (got ' + (data ? data.length : 0) + ' bytes)' };
    } catch (e) {
        return { ok: false, error: e.message };
    }
});

ipcMain.handle('get-tile-server-port', () => 18321);

// ========== MARKERS/DRAWINGS FILE I/O ==========
ipcMain.handle('load-markers-file', async (e, filePath) => {
    try {
        if (fs.existsSync(filePath)) {
            const data = fs.readFileSync(filePath, 'utf8');
            return JSON.parse(data);
        }
    } catch (e) {}
    return [];
});

ipcMain.handle('save-markers-file', async (e, filePath, data) => {
    console.log('=== SAVE MARKERS ===', filePath, data?.length);
    try {
        const dir = path.dirname(filePath);
        if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
        const json = JSON.stringify(data, null, 2);
        fs.writeFileSync(filePath, json, 'utf8');
        const verify = fs.readFileSync(filePath, 'utf8');
        console.log('=== SAVED OK ===', verify.length);
        return verify.length === json.length;
    } catch (e) {
        console.error('[MARKERS] Save error:', e.message, 'Path:', filePath);
        return false;
    }
});

ipcMain.handle('save-drawings-file', async (e, filePath, data) => {
    try {
        const dir = path.dirname(filePath);
        if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
        const json = JSON.stringify(data, null, 2);
        fs.writeFileSync(filePath, json, 'utf8');
        return true;
    } catch (e) {
        console.error('[DRAWINGS] Save error:', e.message);
        return false;
    }
});

ipcMain.handle('load-drawings-file', async (e, filePath) => {
    try {
        if (fs.existsSync(filePath)) {
            const data = fs.readFileSync(filePath, 'utf8');
            return JSON.parse(data);
        }
    } catch (e) {}
    return null;
});

ipcMain.handle('github-load-file', async (e, repo, token, filePath) => {
    const [owner, repoName] = repo.split('/');
    try {
        const data = await fetchGitHubFile(token || '', owner, repoName, filePath);
        if (data) {
            return data.toString('utf8');
        }
    } catch (e) {}
    return null;
});

ipcMain.handle('open-external', (e, url) => shell.openExternal(url));

// ========== TILE CREATION ==========
ipcMain.handle('create-empty-tiles', async (e, destFolder) => {
    const tilesDir = path.join(destFolder, 'tiles');
    const stages = ['0', '-1', '-2', '-3', '-4', '-5', '-6'];
    for (const s of stages) {
        const dir = path.join(tilesDir, s);
        if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    }
    return tilesDir;
});

ipcMain.handle('select-source-folder', async () => {
    const result = await dialog.showOpenDialog(mainWindow, {
        properties: ['openDirectory'],
        title: 'Select folder with tile PNGs'
    });
    if (!result.canceled && result.filePaths.length > 0) {
        return { path: result.filePaths[0] };
    }
    return null;
});

ipcMain.handle('select-dest-folder', async () => {
    const result = await dialog.showOpenDialog(mainWindow, {
        properties: ['openDirectory'],
        title: 'Select output folder for tiles'
    });
    if (!result.canceled && result.filePaths.length > 0) {
        return { path: result.filePaths[0] };
    }
    return null;
});

ipcMain.handle('auto-match-tile', async (e, { tilePath, mapFolder }) => {
    try {
        const tilesDir = path.join(mapFolder, 'tiles', '0');
        if (!fs.existsSync(tilesDir)) return { found: false };

        const stage0Files = fs.readdirSync(tilesDir).filter(f => /^tile_-?\d+_-?\d+\.webp$/.test(f));
        if (stage0Files.length === 0) return { found: false };

        const userImage = await sharp(tilePath).resize(100, 100).greyscale().raw().toBuffer();
        let bestMatch = null;
        let bestConfidence = 0;

        for (const file of stage0Files) {
            const match = file.match(/tile_(-?\d+)_(-?\d+)/);
            if (!match) continue;
            const sx = parseInt(match[1]), sy = parseInt(match[2]);

            const stage0Image = await sharp(path.join(tilesDir, file)).resize(400, 400).greyscale().raw().toBuffer();

            for (let gy = 0; gy < 4; gy++) {
                for (let gx = 0; gx < 4; gx++) {
                    // Extract 100x100 section from stage0
                    let sectionError = 0;
                    for (let py = 0; py < 100; py++) {
                        for (let px = 0; px < 100; px++) {
                            const idx = (py * 400 + px) * 1;
                            const userIdx = (py * 100 + px) * 1;
                            const diff = Math.abs(stage0Image[idx] - userImage[userIdx]);
                            sectionError += diff * diff;
                        }
                    }
                    const mse = sectionError / (100 * 100);
                    const confidence = Math.max(0, 100 - (mse / 15));

                    if (confidence > bestConfidence) {
                        bestConfidence = confidence;
                        bestMatch = {
                            tileX: sx * 4 + gx,
                            tileY: sy * 4 + gy,
                            stage0Tile: file,
                            confidence: Math.round(confidence * 100) / 100
                        };
                    }
                }
            }
        }

        if (bestMatch && bestMatch.confidence > 30) {
            return { found: true, ...bestMatch };
        }
        return { found: false };
    } catch (err) {
        return { found: false, error: err.message };
    }
});

ipcMain.handle('get-preview-tile', async (e, folder) => {
    // Find tile_0_0.png or similar
    const files = fs.readdirSync(folder);
    const tile00 = files.find(f => /^tile[_\-]?0[_\-]?0\.(png|webp|jpg)$/i.test(f));
    if (!tile00) {
        // Just get the first tile file
        const firstTile = files.find(f => /\.(png|webp|jpg)$/i.test(f) && /tile/i.test(f));
        if (!firstTile) return null;
        const data = fs.readFileSync(path.join(folder, firstTile));
        return { filename: firstTile, data: data.toString('base64') };
    }
    const data = fs.readFileSync(path.join(folder, tile00));
    return { filename: tile00, data: data.toString('base64') };
});

ipcMain.handle('scan-tiles', async (e, folder) => {
    const files = fs.readdirSync(folder);
    const tiles = files.filter(f => /\.(png|webp|jpg)$/i.test(f) && /tile/i.test(f));
    const parsed = [];
    for (const f of tiles) {
        const match = f.match(/tile[_\-](-?\d+)[_\-](-?\d+)\./i);
        if (match) {
            parsed.push({ filename: f, x: parseInt(match[1]), y: parseInt(match[2]) });
        }
    }
    return { count: parsed.length, tiles: parsed };
});

ipcMain.handle('process-tiles', async (e, { sourceFolder, destFolder, offsetX, offsetY, mode }) => {
    const sendProgress = (pct, msg) => {
        mainWindow.webContents.send('create-progress', { pct, msg });
    };

    try {
        // Stop tile server first to release all file locks
        if (tileServer) {
            tileServer.close();
            tileServer = null;
        }

        // Scan source tiles
        sendProgress(0, 'Scanning source tiles...');
        const files = fs.readdirSync(sourceFolder);
        const tileFiles = files.filter(f => /\.(png|webp|jpg)$/i.test(f) && /tile/i.test(f));
        const tiles = [];
        for (const f of tileFiles) {
            const match = f.match(/tile[_\-](-?\d+)[_\-](-?\d+)\./i);
            if (match) {
                tiles.push({ filename: f, x: parseInt(match[1]) + offsetX, y: parseInt(match[2]) + offsetY });
            }
        }

        if (tiles.length === 0) {
            sendProgress(100, 'No valid tiles found!');
            return { success: false, error: 'No valid tiles found' };
        }

        sendProgress(5, `Found ${tiles.length} tiles. Grouping into stage 0...`);

        // Group tiles into 4x4 blocks for stage 0
        const stage0Tiles = {};
        for (const tile of tiles) {
            const stage0X = Math.floor(tile.x / 4);
            const stage0Y = Math.floor(tile.y / 4);
            const key = `${stage0X}_${stage0Y}`;
            if (!stage0Tiles[key]) stage0Tiles[key] = { x: stage0X, y: stage0Y, tiles: [] };
            stage0Tiles[key].tiles.push(tile);
        }

        const totalGroups = Object.keys(stage0Tiles).length;
        sendProgress(10, `Creating ${totalGroups} stage 0 tiles...`);

        // Create output directories - check if destFolder IS already a tiles folder
        let tilesDir;
        const hasStage0 = fs.existsSync(path.join(destFolder, '0')) || fs.existsSync(path.join(destFolder, '-1'));
        if (hasStage0) {
            // destFolder IS the tiles folder
            tilesDir = destFolder;
        } else {
            // destFolder is parent, create tiles/ inside
            tilesDir = path.join(destFolder, 'tiles');
            if (!fs.existsSync(tilesDir)) fs.mkdirSync(tilesDir, { recursive: true });
        }
        const stage0Dir = path.join(tilesDir, '0');
        if (!fs.existsSync(stage0Dir)) fs.mkdirSync(stage0Dir, { recursive: true });

        // Process each stage 0 tile
        let processed = 0;
        for (const key of Object.keys(stage0Tiles)) {
            const group = stage0Tiles[key];
            const pct = 10 + (processed / totalGroups) * 60;
            sendProgress(pct, `Compositing tile ${processed + 1}/${totalGroups}...`);

            // Create 400x400 canvas
            const canvas = sharp({ create: { width: 400, height: 400, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 1 } } });

            const composites = [];
            for (const tile of group.tiles) {
                const localX = tile.x - (group.x * 4);
                const localY = tile.y - (group.y * 4);
                const srcPath = path.join(sourceFolder, tile.filename);
                if (fs.existsSync(srcPath)) {
                    composites.push({
                        input: srcPath,
                        left: localX * 100,
                        top: localY * 100
                    });
                }
            }

            if (composites.length > 0) {
                const outPath = path.join(stage0Dir, `tile_${group.x}_${group.y}.webp`);
                let canvas;
                if (mode === 'add' && fs.existsSync(outPath)) {
                    try {
                        const tmpRead = path.join(stage0Dir, `._tmpread.webp`);
                        fs.copyFileSync(outPath, tmpRead);
                        const existingBuffer = await sharp(tmpRead).resize(400, 400).toBuffer();
                        canvas = sharp(existingBuffer);
                        try { fs.unlinkSync(tmpRead); } catch (e) {}
                    } catch (readErr) {
                        // Can't read existing file - skip this tile to preserve it
                        console.error('[MERGE] Skipping tile (file locked):', outPath);
                        continue;
                    }
                } else {
                    canvas = sharp({ create: { width: 400, height: 400, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 1 } } });
                }
                const buf = await canvas.composite(composites).webp({ quality: 80 }).toBuffer();
                fs.writeFileSync(outPath, buf);
            }
            processed++;
        }

        // Generate higher zoom stages (stages -1, -2, etc.)
        sendProgress(75, 'Generating zoom stages...');
        let prevStageDir = stage0Dir;
        let stageNum = 1;

        while (true) {
            const prevFiles = fs.readdirSync(prevStageDir).filter(f => /^tile_.*\.webp$/.test(f));
            if (prevFiles.length <= 1) break; // No more downsampling needed

            const stageDir = path.join(tilesDir, `-${stageNum}`);
            if (!fs.existsSync(stageDir)) fs.mkdirSync(stageDir, { recursive: true });

            // Group prev stage tiles into 2x2 blocks
            const prevGroups = {};
            for (const f of prevFiles) {
                const m = f.match(/tile_(-?\d+)_(-?\d+)\.webp/);
                if (!m) continue;
                const px = parseInt(m[1]), py = parseInt(m[2]);
                const gx = Math.floor(px / 2);
                const gy = Math.floor(py / 2);
                const gkey = `${gx}_${gy}`;
                if (!prevGroups[gkey]) prevGroups[gkey] = { x: gx, y: gy, tiles: [] };
                prevGroups[gkey].tiles.push({ path: path.join(prevStageDir, f), localX: px - gx * 2, localY: py - gy * 2 });
            }

            let gIdx = 0;
            const gTotal = Object.keys(prevGroups).length;
            for (const gkey of Object.keys(prevGroups)) {
                const group = prevGroups[gkey];
                const pct = 75 + (stageNum - 1) * 5 + (gIdx / gTotal) * 5;
                sendProgress(Math.min(pct, 95), `Stage -${stageNum}: tile ${gIdx + 1}/${gTotal}...`);

                const canvas = sharp({ create: { width: 400, height: 400, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 1 } } });
                
                // Resize each source tile to 200x200 before compositing
                const composites = [];
                for (const t of group.tiles) {
                    const resized = await sharp(t.path).resize(200, 200).toBuffer();
                    composites.push({
                        input: resized,
                        left: t.localX * 200,
                        top: t.localY * 200
                    });
                }

                const outPath = path.join(stageDir, `tile_${group.x}_${group.y}.webp`);
                try {
                    const buf = await canvas.composite(composites).webp({ quality: 80 }).toBuffer();
                    fs.writeFileSync(outPath, buf);
                } catch (writeErr) {
                    console.error('[ZOOM] Write failed:', writeErr.message);
                }
                gIdx++;
            }

            prevStageDir = stageDir;
            stageNum++;
            if (stageNum > 6) break; // Max 6 zoom stages
        }

        sendProgress(100, 'Done!');
        // Restart tile server
        startTileServer();
        return { success: true, tiles: tiles.length, stages: stageNum };

    } catch (err) {
        sendProgress(100, 'Error: ' + err.message);
        // Restart tile server even on error
        startTileServer();
        return { success: false, error: err.message };
    }
});

app.whenReady().then(createWindow);
app.on('window-all-closed', () => {
    if (tileServer) tileServer.close();
    app.quit();
});