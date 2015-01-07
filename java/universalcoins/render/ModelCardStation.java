package universalcoins.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCardStation extends ModelBase {
  //fields
    ModelRenderer left_side;
    ModelRenderer right_side;
    ModelRenderer bottom;
    ModelRenderer top;
    ModelRenderer face_top;
    ModelRenderer back;
    ModelRenderer face_bottom;
    ModelRenderer face_right;
    ModelRenderer face_left;
    ModelRenderer monitor;
    ModelRenderer cubby_top;
    ModelRenderer cubby_left;
    ModelRenderer cubby_right;
    ModelRenderer cubby_bottom;
  
  public ModelCardStation()
  {
    textureWidth = 32;
    textureHeight = 32;
    
      left_side = new ModelRenderer(this, 0, 0);
      left_side.addBox(0F, 0F, 0F, 16, 16, 0);
      left_side.setRotationPoint(-8F, 8F, -8F);
      left_side.setTextureSize(32, 32);
      left_side.mirror = true;
      setRotation(left_side, 0F, -1.570796F, 0F);
      right_side = new ModelRenderer(this, 0, 0);
      right_side.addBox(0F, 0F, -16F, 16, 16, 0);
      right_side.setRotationPoint(-8F, 8F, -8F);
      right_side.setTextureSize(32, 32);
      right_side.mirror = true;
      setRotation(right_side, 0F, -1.570796F, 0F);
      bottom = new ModelRenderer(this, 0, 0);
      bottom.addBox(0F, 0F, 0F, 16, 16, 0);
      bottom.setRotationPoint(-8F, 24F, -8F);
      bottom.setTextureSize(32, 32);
      bottom.mirror = true;
      setRotation(bottom, 1.570796F, 0F, 0F);
      top = new ModelRenderer(this, 0, 0);
      top.addBox(0F, 0F, 0F, 16, 16, 0);
      top.setRotationPoint(-8F, 8F, -8F);
      top.setTextureSize(32, 32);
      top.mirror = true;
      setRotation(top, 1.570796F, 0F, 0F);
      face_top = new ModelRenderer(this, 0, 0);
      face_top.addBox(0F, 0F, 0F, 16, 2, 0);
      face_top.setRotationPoint(-8F, 8F, -8F);
      face_top.setTextureSize(32, 32);
      face_top.mirror = true;
      setRotation(face_top, 0F, 0F, 0F);
      back = new ModelRenderer(this, 0, 0);
      back.addBox(0F, 0F, 0F, 16, 16, 0);
      back.setRotationPoint(-8F, 8F, 8F);
      back.setTextureSize(32, 32);
      back.mirror = true;
      setRotation(back, 0F, 0F, 0F);
      face_bottom = new ModelRenderer(this, 0, 0);
      face_bottom.addBox(0F, 0F, 0F, 16, 2, 0);
      face_bottom.setRotationPoint(-8F, 22F, -8F);
      face_bottom.setTextureSize(32, 32);
      face_bottom.mirror = true;
      setRotation(face_bottom, 0F, 0F, 0F);
      face_right = new ModelRenderer(this, 0, 0);
      face_right.addBox(0F, 0F, 0F, 2, 12, 0);
      face_right.setRotationPoint(6F, 10F, -8F);
      face_right.setTextureSize(32, 32);
      face_right.mirror = true;
      setRotation(face_right, 0F, 0F, 0F);
      face_left = new ModelRenderer(this, 0, 0);
      face_left.addBox(0F, 0F, 0F, 2, 12, 0);
      face_left.setRotationPoint(-8F, 10F, -8F);
      face_left.setTextureSize(32, 32);
      face_left.mirror = true;
      setRotation(face_left, 0F, 0F, 0F);
      monitor = new ModelRenderer(this, 0, 16);
      monitor.addBox(0F, 0F, 0F, 12, 8, 0);
      monitor.setRotationPoint(-6F, 11F, -4F);
      monitor.setTextureSize(32, 32);
      monitor.mirror = true;
      setRotation(monitor, 0F, 0F, 0F);
      cubby_top = new ModelRenderer(this, 0, 0);
      cubby_top.addBox(0F, 0F, 0F, 12, 4, 0);
      cubby_top.setRotationPoint(-6F, 10F, -8F);
      cubby_top.setTextureSize(32, 32);
      cubby_top.mirror = true;
      setRotation(cubby_top, 1.238461F, 0F, 0F);
      cubby_left = new ModelRenderer(this, 0, 0);
      cubby_left.addBox(0F, 0F, 0F, 12, 4, 0);
      cubby_left.setRotationPoint(-6F, 10F, -4F);
      cubby_left.setTextureSize(32, 32);
      cubby_left.mirror = true;
      setRotation(cubby_left, -1.570796F, 0F, 1.570796F);
      cubby_right = new ModelRenderer(this, 0, 0);
      cubby_right.addBox(0F, 0F, 0F, 12, 4, 0);
      cubby_right.setRotationPoint(6F, 10F, -8F);
      cubby_right.setTextureSize(32, 32);
      cubby_right.mirror = true;
      setRotation(cubby_right, 1.570796F, 0F, 1.570796F);
      cubby_bottom = new ModelRenderer(this, 0, 24);
      cubby_bottom.addBox(0F, 0F, 0F, 12, 5, 0);
      cubby_bottom.setRotationPoint(-6F, 19F, -4F);
      cubby_bottom.setTextureSize(32, 32);
      cubby_bottom.mirror = true;
      setRotation(cubby_bottom, -0.9250245F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    left_side.render(f5);
    right_side.render(f5);
    bottom.render(f5);
    top.render(f5);
    face_top.render(f5);
    back.render(f5);
    face_bottom.render(f5);
    face_right.render(f5);
    face_left.render(f5);
    monitor.render(f5);
    cubby_top.render(f5);
    cubby_left.render(f5);
    cubby_right.render(f5);
    cubby_bottom.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
  
  public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
  {
    super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }

}
