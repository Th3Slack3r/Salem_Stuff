<?php
$phpVersion = phpversion();
?>

<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Silly Development - Nginx</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
        }
    </script>
</head>
<body class="bg-gray-900 text-gray-100 min-h-screen flex items-center justify-center px-4">

    <div class="bg-gray-800 rounded-2xl shadow-2xl p-8 max-w-xl w-full text-center space-y-6">
        <h1 class="text-3xl font-bold text-white">✅ Webserver Installed Successfully</h1>

        <p class="text-lg text-gray-300">Your current PHP version is:</p>
        <div class="bg-blue-600 text-white text-xl py-2 px-6 rounded-md inline-block font-mono">
            <?php echo $phpVersion; ?>
        </div>

        <p class="text-green-400 text-lg font-semibold">
            Everything is up and running!
        </p>

        <div class="bg-yellow-100 text-yellow-800 text-sm font-medium p-4 rounded-md border border-yellow-300">
            ⚠️ Please upload your web files to the <code class="bg-yellow-200 px-1 py-0.5 rounded font-mono">webroot</code> folder.
        </div>

        <footer class="text-sm text-gray-400 pt-4">
            Powered by <a href="https://sillydev.co.uk" target="_blank" class="text-blue-400 hover:underline">Silly Development</a>
        </footer>
    </div>

</body>
</html>
