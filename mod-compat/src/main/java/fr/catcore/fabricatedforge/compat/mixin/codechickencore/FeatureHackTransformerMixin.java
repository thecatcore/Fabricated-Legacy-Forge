package fr.catcore.fabricatedforge.compat.mixin.codechickencore;

import codechicken.core.asm.FeatureHackTransformer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fr.catcore.fabricatedforge.Constants;
import io.github.fabriccompatibiltylayers.modremappingapi.api.MappingUtils;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FeatureHackTransformer.class)
public class FeatureHackTransformerMixin {
    @WrapOperation(method = {"transformer004", "transformer002"}, require = 0, at = @At(value = "NEW", target = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/objectweb/asm/tree/FieldInsnNode;"), remap = false)
    private FieldInsnNode flf$remapField(int opcode, String owner, String name, String descriptor, Operation<FieldInsnNode> original) {
        String remappedOwner = Constants.mapClass(owner);
        MappingUtils.ClassMember member = Constants.mapFieldFromRemappedClass(remappedOwner, name, descriptor);

        return original.call(opcode, remappedOwner, member.name, Constants.mapTypeDescriptor(member.desc));
    }

    @WrapOperation(method = {"transformer001", "transformer003", "transformer004"}, require = 0, at = @At(value = "NEW", target = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/objectweb/asm/tree/MethodInsnNode;"), remap = false)
    private MethodInsnNode flf$remapMethod(int opcode, String owner, String name, String descriptor, Operation<MethodInsnNode> original) {
        String remappedOwner = Constants.mapClass(owner);
        MappingUtils.ClassMember member = Constants.mapFieldFromRemappedClass(remappedOwner, name, descriptor);

        return original.call(opcode, remappedOwner, member.name, Constants.mapMethodDescriptor(member.desc));
    }
}
