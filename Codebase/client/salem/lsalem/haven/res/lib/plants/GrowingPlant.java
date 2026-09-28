package haven.res.lib.plants;

import haven.Config;
import haven.Coord3f;
import haven.FastMesh;
import haven.Gob;
import haven.Material;
import haven.MeshBuf;
import haven.Message;
import haven.RenderList;
import haven.Rendered;
import haven.Resource;
import haven.Sprite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;

public class GrowingPlant extends Sprite {
   private Rendered[] parts;
   private FastMesh[] meshes;

   private void cons(Gob gob, Resource res, int paramInt1, int paramInt2) {
      ArrayList<Integer> meshes = new ArrayList<>();
      Collection<FastMesh.MeshRes> allMeshes = res.layers(FastMesh.MeshRes.class);

      for (FastMesh.MeshRes mesh : allMeshes) {
         if (mesh.id == paramInt2 && mesh.mat != null && !meshes.contains(mesh.ref)) {
            meshes.add(mesh.ref);
         }
      }

      HashMap<Material, MeshBuf> mats = new HashMap<>();
      Object rand = gob.mkrandoom();
      float f1 = gob.glob.map.getcz(gob.rc);
      int plantcount = Config.fieldfix ? 1 : paramInt1;

      for (int i = 0; i < plantcount; i++) {
         float offsetx = Config.fieldfix ? 0.0F : ((Random)rand).nextFloat() * 44.0F - 22.0F;
         float offsety = Config.fieldfix ? 0.0F : ((Random)rand).nextFloat() * 44.0F - 22.0F;
         Coord3f localCoord3f = new Coord3f(offsetx, offsety, gob.glob.map.getcz(gob.rc.x + offsetx, gob.rc.y + offsety) - f1);
         double d = Config.fieldfix ? 0.0 : ((Random)rand).nextDouble() * Math.PI * 2.0;
         float f2 = (float)Math.sin(d);
         float f3 = (float)Math.cos(d);
         if (!meshes.isEmpty()) {
            int j = meshes.get(((Random)rand).nextInt(meshes.size()));

            for (FastMesh.MeshRes localMeshRes : allMeshes) {
               if (localMeshRes.ref == j) {
                  MeshBuf localMeshBuf = mats.get(localMeshRes.mat.get());
                  if (localMeshBuf == null) {
                     mats.put(localMeshRes.mat.get(), localMeshBuf = new MeshBuf());
                  }

                  MeshBuf.Vertex[] arrayOfVertex1 = localMeshBuf.copy(localMeshRes.m);

                  for (MeshBuf.Vertex localVertex : arrayOfVertex1) {
                     localVertex.pos.x = localVertex.pos.x * Config.fieldproducescale;
                     localVertex.pos.y = localVertex.pos.y * Config.fieldproducescale;
                     localVertex.pos.z = localVertex.pos.z * Config.fieldproducescale;
                     if (!Config.fieldfix) {
                        float f6 = localVertex.pos.x;
                        float f7 = localVertex.pos.y;
                        localVertex.pos.x = f6 * f3 - f7 * f2;
                        localVertex.pos.y = f7 * f3 + f6 * f2;
                     }

                     localVertex.pos.x = localVertex.pos.x + localCoord3f.x;
                     localVertex.pos.y = localVertex.pos.y - localCoord3f.y;
                     localVertex.pos.z = localVertex.pos.z + localCoord3f.z;
                     float f8 = localVertex.nrm.x;
                     float f9 = localVertex.nrm.y;
                     localVertex.nrm.x = f8 * f3 - f9 * f2;
                     localVertex.nrm.y = f9 * f3 - f8 * f2;
                  }
               }
            }
         }
      }

      this.meshes = new FastMesh[mats.size()];
      this.parts = new Rendered[mats.size()];
      int ix = 0;

      for (Entry localEntry : mats.entrySet()) {
         this.meshes[ix] = ((MeshBuf)localEntry.getValue()).mkmesh();
         this.parts[ix] = ((Material)localEntry.getKey()).apply(this.meshes[ix]);
         ix++;
      }
   }

   public GrowingPlant(Gob paramGob, Resource paramResource, int paramInt1, int paramInt2) {
      super(paramGob, paramResource);
      this.cons(paramGob, paramResource, paramInt1, paramInt2);
   }

   @Override
   public boolean setup(RenderList paramRenderList) {
      for (Rendered localRendered : this.parts) {
         paramRenderList.add(localRendered, null);
      }

      return false;
   }

   @Override
   public void dispose() {
      for (FastMesh localFastMesh : this.meshes) {
         localFastMesh.dispose();
      }
   }

   public static class Factory implements Sprite.Factory {
      private final int sn;

      public Factory(int paramInt) {
         this.sn = paramInt;
      }

      @Override
      public Sprite create(Sprite.Owner paramOwner, Resource paramResource, Message paramMessage) {
         return new GrowingPlant((Gob)paramOwner, paramResource, this.sn, paramMessage.uint8());
      }
   }
}
