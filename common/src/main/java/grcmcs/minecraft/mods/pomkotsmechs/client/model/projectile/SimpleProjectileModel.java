package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.Entity;

/** Lightweight, non-GeckoLib model used by simple projectile entities. */
public class SimpleProjectileModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart root;

    public SimpleProjectileModel(ModelPart root) {
        this.root = root.getChild("root");
    }

    public static LayerDefinition rifleLayer() {
        return boxLayer(-4.0F, -4.0F, -20.0F, 8.0F, 8.0F, 39.0F, 32, 32);
    }

    public static LayerDefinition machineLayer() {
        return boxLayer(-5.0F, -5.0F, -16.0F, 10.0F, 10.0F, 31.0F, 16, 16);
    }

    public static LayerDefinition grenadeLayer() {
        return boxLayer(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, 32, 32);
    }

    private static LayerDefinition boxLayer(float x, float y, float z,
                                            float width, float height, float depth,
                                            int textureWidth, int textureHeight) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("root",
                CubeListBuilder.create().texOffs(0, 0).addBox(x, y, z, width, height, depth),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
