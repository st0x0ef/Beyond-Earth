package net.mrscauthd.beyond_earth.common.blocks.entities.uranium;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class UraniumBaseBlockEntity extends BlockEntity  {

    public UraniumBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        AABB area = new AABB(this.getBlockPos()).inflate(5);

        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            entity.setSecondsOnFire(10);
        }
    }
}
