package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.StrayClothingLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.world.capabilities.entitypatch.mob.SkeletonPatch;

@OnlyIn(Dist.CLIENT)
public class PStrayRenderer extends PHumanoidRenderer<PathfinderMob, SkeletonPatch<PathfinderMob>, HumanoidModel<PathfinderMob>, HumanoidMobRenderer<PathfinderMob, HumanoidModel<PathfinderMob>>, HumanoidMesh> {
	public PStrayRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
		super(() -> Meshes.SKELETON, context, entityType);
		this.addPatchedLayer(StrayClothingLayer.class, new EmptyLayer<>());
	}
}