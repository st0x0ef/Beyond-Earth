package net.mrscauthd.beyond_earth.registries;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mrscauthd.beyond_earth.BeyondEarth;

import java.util.List;

public class LevelRegistry {
    /** MOON */
    public static final ResourceKey<Level> MOON = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "moon"));
    public static final ResourceKey<Level> MOON_ORBIT = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "moon_orbit"));

    /** MARS */
    public static final ResourceKey<Level> MARS = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "mars"));
    public static final ResourceKey<Level> MARS_ORBIT = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "mars_orbit"));

    /** MERCURY */
    public static final ResourceKey<Level> MERCURY = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "mercury"));
    public static final ResourceKey<Level> MERCURY_ORBIT = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "mercury_orbit"));

    /** VENUS */
    public static final ResourceKey<Level> VENUS = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "venus"));
    public static final ResourceKey<Level> VENUS_ORBIT = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "venus_orbit"));

    /** GLACIO */
    public static final ResourceKey<Level> GLACIO = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "glacio"));
    public static final ResourceKey<Level> GLACIO_ORBIT = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID, "glacio_orbit"));

    /** EARTH */
    public static final ResourceKey<Level> EARTH = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<Level> EARTH_ORBIT = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(BeyondEarth.MODID,"earth_orbit"));

    public static List<ResourceKey<Level>> WORLDS_WITHOUT_RAIN = List.of(
            MOON,
            MOON_ORBIT,
            MARS_ORBIT,

            MERCURY,
            MERCURY_ORBIT,
            VENUS_ORBIT,
            GLACIO_ORBIT,
            EARTH_ORBIT
    );

    public static List<ResourceKey<Level>> WORLDS_WITHOUT_OXYGEN = List.of(
            MOON,
            MOON_ORBIT,
            MARS,
            MARS_ORBIT,
            MERCURY,
            MERCURY_ORBIT,
            VENUS,
            VENUS_ORBIT,
            GLACIO_ORBIT,
            EARTH_ORBIT
    );

    public static List<ResourceKey<Level>> SPACE_WORLDS = List.of(
            MOON,
            MOON_ORBIT,
            MARS,
            MARS_ORBIT,
            MERCURY,
            MERCURY_ORBIT,
            VENUS,
            VENUS_ORBIT,
            GLACIO,
            GLACIO_ORBIT,
            EARTH_ORBIT
    );

    public static List<Pair<ResourceKey<Level>, ResourceKey<Level>>> WORLDS_WITH_ORBITS = List.of(
            new Pair<>(MOON, MOON_ORBIT),
            new Pair<>(MARS, MARS_ORBIT),
            new Pair<>(MERCURY, MERCURY_ORBIT),
            new Pair<>(VENUS, VENUS_ORBIT),
            new Pair<>(GLACIO, GLACIO_ORBIT),
            new Pair<>(MOON, MOON_ORBIT),
            new Pair<>(EARTH, EARTH_ORBIT)
    );

    public static List<ResourceKey<Level>> ORBITS = List.of(
            EARTH_ORBIT,
            MOON_ORBIT,
            MARS_ORBIT,
            MERCURY_ORBIT,
            VENUS_ORBIT,
            GLACIO_ORBIT
    );
}
