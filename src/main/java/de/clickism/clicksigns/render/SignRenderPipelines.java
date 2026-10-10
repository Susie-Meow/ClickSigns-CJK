package de.clickism.clicksigns.render;

import net.minecraft.resources.Identifier;

import de.clickism.clicksigns.ClickSigns;
import net.minecraft.client.renderer.rendertype.RenderType;

//? if >=26.1 {
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
//? if >=26.2
import net.minecraft.client.renderer.BindGroupLayouts;

//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
//?} else {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
*///?}
//?}

public class SignRenderPipelines {

    private static final Identifier WHITE = ClickSigns.identifier("textures/misc/white.png");

    //? if >=26.1 {
    public static final RenderPipeline SIGN_BASE =
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(ClickSigns.identifier("pipeline/sign_base"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            //? >=26.2 {
            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
            //?} else
            //.withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(true)
            .withDepthStencilState(depth(true, -1.0f, -2.0f))
            .build();

    public static final RenderPipeline SIGN_ELEMENT =
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(ClickSigns.identifier("pipeline/sign_element"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            //? >=26.2 {
            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
            //?} else
            //.withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(true)
            // Test against the base plane, but never write depth:
            // layers can't punch holes in each other, order decides who is on top
            .withDepthStencilState(depth(false, -1.0f, -4.0f))
            .build();

    private static final Map<Identifier, RenderType> BASE_TYPES = new HashMap<>();
    private static final Map<Identifier, RenderType> ELEMENT_TYPES = new HashMap<>();

    /**
     * Whether the custom sign pipelines may be used at all.
     * <p>
     * Shader mods that replace Minecraft's render pipeline layer (Iris, VanillaPipelines, …)
     * rebuild every program they don't know about. Our pipelines are
     * {@code ENTITY_SNIPPET} plus a {@code ALPHA_CUTOUT} shader define, so when such a mod
     * recompiles {@code clicksigns:pipeline/sign_base} the define is lost, the fragment
     * shader never discards fully transparent texels, and the whole sign quad is blended in
     * as a transparent rectangle. Vanilla flags this in the log as
     * {@code Missing program clicksigns:pipeline/... in override list}.
     * <p>
     * When one of those mods is present we therefore render signs through vanilla's
     * {@code ENTITY_TRANSLUCENT_CULL} pipeline, which is exactly what ClickSigns used on every
     * version before 26.1 anyway - the sign loses its custom per-layer depth handling but it
     * is actually visible.
     */
    private static final boolean USE_CUSTOM_PIPELINES = detectCustomPipelineSupport();

    private static boolean detectCustomPipelineSupport() {
        String[] offenders = {
            "net.irisshaders.iris.Iris",
            "com.moulberry.vanillapipeline.ModdedVanillaPipelines",
        };
        for (String offender : offenders) {
            try {
                Class.forName(offender);
                ClickSigns.LOGGER.warn(
                    "{} is installed, so ClickSigns falls back to vanilla render types for road"
                        + " signs. Without this the custom sign pipelines get recompiled without"
                        + " their shader defines and signs render fully transparent.",
                    offender
                );
                return false;
            } catch (ClassNotFoundException ignored) {
                // Not installed, check the next one
            }
        }
        return true;
    }

    public static void registerAll(Consumer<RenderPipeline> consumer) {
        consumer.accept(SIGN_BASE);
        consumer.accept(SIGN_ELEMENT);
    }

    private static RenderType create(RenderPipeline pipeline, Identifier texture) {
        return RenderType.create(
            "clicksigns_sign_layer",
            RenderSetup.builder(pipeline)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup()
        );
    }

    /**
     * Fallback render type using a vanilla pipeline, for shader mods that break custom ones.
     */
    private static RenderType createVanillaFallback(Identifier texture) {
        return RenderType.create(
            "clicksigns_sign_layer_vanilla",
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup()
        );
    }

    private static RenderType layer(Map<Identifier, RenderType> cache, RenderPipeline pipeline, Identifier texture) {
        return cache.computeIfAbsent(
            texture,
            id -> USE_CUSTOM_PIPELINES ? create(pipeline, id) : createVanillaFallback(id)
        );
    }

    private static DepthStencilState depth(boolean write, float scale, float constant) {
        //? if >=26.2 {
        return new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, write, -scale, -constant);
        //?} else
        //return new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, write, scale, constant);
    }

    //?}

    public static RenderType baseRenderType(Identifier texture) {
        //? if >=26.1 {
        return layer(BASE_TYPES, SIGN_BASE, texture);
        //?} else
        //return RenderType.entityTranslucentCull(texture);
    }

    public static RenderType elementRenderType(Identifier texture) {
        //? if >=26.1 {
        return layer(ELEMENT_TYPES, SIGN_ELEMENT, texture);
        //?} else
        //return RenderType.entityTranslucentCull(texture);
    }

    public static RenderType colorRenderType() {
        //? if >=26.1 {
        return layer(ELEMENT_TYPES, SIGN_ELEMENT, WHITE);
        //?} else
        //return RenderType.entityTranslucentCull(WHITE);
    }
}
