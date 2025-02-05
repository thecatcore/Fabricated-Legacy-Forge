package fr.catcore.fabricatedforge.mixin.forgefml.item;

import cpw.mods.fml.common.registry.ItemProxy;
import fr.catcore.fabricatedforge.mixininterface.IBlock;
import fr.catcore.fabricatedforge.mixininterface.IItem;
import fr.catcore.fabricatedforge.mixininterface.IServerPlayerInteractionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemProxy, IItem {

    @Shadow @Final public int id;
    @Shadow public static Item[] ITEMS;


    @Shadow public abstract float getMiningSpeedMultiplier(ItemStack stack, Block block);

    @Shadow public abstract boolean isDamageable();

    @Environment(EnvType.CLIENT)
    @Shadow public abstract int method_3378(ItemStack itemStack);

    @Environment(EnvType.CLIENT)
    @Shadow public abstract boolean method_3397();

    @Shadow public abstract boolean isFood();

    @Shadow public abstract Item getRecipeRemainder();

    @Unique
    protected boolean canRepair = true;
    @Unique
    public boolean isDefaultTexture = true;
    @Unique
    private String currentTexture = "/gui/items.png";

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fmlCtr(int par1, CallbackInfo ci) {
        if (!((Object)this instanceof BlockItem)) {
            this.isDefaultTexture = "/gui/items.png".equals(this.getTextureFile());
        }
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"), index = 0)
    private String fmlCtr(String x) {
        return "CONFLICT @ " + (this.id - 256) + " item slot already occupied by " + ITEMS[this.id] + " while adding " + this;
    }

    /**
     * @author Minecraft Forge
     * @reason none
     */
    @Overwrite
    public BlockHitResult onHit(World par1World, PlayerEntity par2EntityPlayer, boolean par3) {
        float var4 = 1.0F;
        float var5 = par2EntityPlayer.prevPitch + (par2EntityPlayer.pitch - par2EntityPlayer.prevPitch) * var4;
        float var6 = par2EntityPlayer.prevYaw + (par2EntityPlayer.yaw - par2EntityPlayer.prevYaw) * var4;
        double var7 = par2EntityPlayer.prevX + (par2EntityPlayer.x - par2EntityPlayer.prevX) * (double)var4;
        double var9 = par2EntityPlayer.prevY + (par2EntityPlayer.y - par2EntityPlayer.prevY) * (double)var4 + 1.62 - (double)par2EntityPlayer.heightOffset;
        double var11 = par2EntityPlayer.prevZ + (par2EntityPlayer.z - par2EntityPlayer.prevZ) * (double)var4;
        Vec3d var13 = Vec3d.method_603().getOrCreate(var7, var9, var11);
        float var14 = MathHelper.cos(-var6 * 0.017453292F - 3.1415927F);
        float var15 = MathHelper.sin(-var6 * 0.017453292F - 3.1415927F);
        float var16 = -MathHelper.cos(-var5 * 0.017453292F);
        float var17 = MathHelper.sin(-var5 * 0.017453292F);
        float var18 = var15 * var16;
        float var20 = var14 * var16;
        double var21 = 5.0;
        if (par2EntityPlayer instanceof ServerPlayerEntity) {
            var21 = ((IServerPlayerInteractionManager)((ServerPlayerEntity)par2EntityPlayer).interactionManager).getBlockReachDistance();
        }

        Vec3d var23 = var13.method_613((double)var18 * var21, (double)var17 * var21, (double)var20 * var21);
        return par1World.rayTrace(var13, var23, par3, !par3);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
        return true;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        return this.onItemUseFirst(stack, player, world, x, y, z, side);
    }

    /** @deprecated */
    @Deprecated
    @Override
    public boolean onItemUseFirst(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public float getStrVsBlock(ItemStack itemstack, Block block, int metadata) {
        return this.getMiningSpeedMultiplier(itemstack, block);
    }

    @Override
    public boolean isRepairable() {
        return this.canRepair && this.isDamageable();
    }

    @Override
    public Item setNoRepair() {
        this.canRepair = false;
        return (Item)(Object) this;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, PlayerEntity player) {
        return false;
    }

    @Override
    public void onUsingItemTick(ItemStack stack, PlayerEntity player, int count) {
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        return false;
    }

    @Override
    public int getIconIndex(ItemStack stack, int renderPass, PlayerEntity player, ItemStack usingItem, int useRemaining) {
        return this.method_3378(stack);
    }

    @Override
    public int getRenderPasses(int metadata) {
        return this.method_3397() ? 2 : 1;
    }

    @Override
    public String getTextureFile() {
        return (Object)this instanceof BlockItem ? ((IBlock)Block.BLOCKS[((BlockItem)(Object)this).method_3464()]).getTextureFile() : this.currentTexture;
    }

    @Override
    public void setTextureFile(String texture) {
        this.currentTexture = texture;
        this.isDefaultTexture = false;
    }

    @Override
    public ItemStack getContainerItemStack(ItemStack itemStack) {
        return !this.isFood() ? null : new ItemStack(this.getRecipeRemainder());
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return 6000;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return false;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        return null;
    }

    @Override
    public boolean isDefaultTexture() {
        return this.isDefaultTexture;
    }

    @Override
    public void isDefaultTexture(boolean isDefaultTexture) {
        this.isDefaultTexture = isDefaultTexture;
    }
}
