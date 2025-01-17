package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.util.ListIndexedIterable;
import com.ishland.c2me.mixin.access.IMultiNoiseBiomeSource;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class GlobalCachingMultiNoiseBiomeSource extends MultiNoiseBiomeSource implements IGlobalBiomeCache {

    private volatile MultiBiomeCache multiBiomeCache = null;

    public GlobalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<Supplier<Biome>> entries) {
        super(entries);
        initCache();
    }

    public GlobalCachingMultiNoiseBiomeSource(MultiNoiseUtil.Entries<Supplier<Biome>> entries, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<MultiNoiseBiomeSource.Instance> optional) {
        super(entries, optional);
        initCache();
    }

    private void initCache() {
        final List<Biome> biomes = ((IMultiNoiseBiomeSource) this).getBiomeEntries().getEntries().stream()
                .map(Pair::getSecond).toList();
        this.multiBiomeCache = new MultiBiomeCache(super::getBiome, new ListIndexedIterable<>(biomes));
    }

    @Override
    public Biome getBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (this.multiBiomeCache != null && !multiNoiseSampler.getClass().isSynthetic() && !multiNoiseSampler.getClass().isHidden()) {
            return this.multiBiomeCache.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, multiNoiseSampler, false);
        } else {
            return super.getBiome(biomeX, biomeY, biomeZ, multiNoiseSampler);
        }
    }

    @Override
    public Biome getBiomeForNoiseGenFast(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (this.multiBiomeCache != null && !multiNoiseSampler.getClass().isSynthetic() && !multiNoiseSampler.getClass().isHidden()) {
            return this.multiBiomeCache.getBiomeForNoiseGen(biomeX, biomeY, biomeZ, multiNoiseSampler, true);
        } else {
            return super.getBiome(biomeX, biomeY, biomeZ, multiNoiseSampler);
        }
    }

    @Override
    public Biome[][][] preloadBiomes(ChunkSectionPos pos, Biome[][][] def, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
        if (this.multiBiomeCache != null) {
            return this.multiBiomeCache.preloadBiomes(pos, def, multiNoiseSampler);
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }
}
