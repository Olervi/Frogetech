package net.frogenet.frogetech.tier;

public enum MachineTier {
    BASIC(
            1,          // Tier
            "Basic",    // Name
            10_000,     // Prod_Buffer
            20,         // Output
            1.0f,       // Efficiency
            5_000,      // Con_Buffer
            20,         // Con_Consumption
            1.0f,       // Con_Speed
            100         // Cable Transfer
    ),
    ADVANCED(
            2,
            "Advanced",
            50_000,
            80,
            1.5f,
            25_000,
            20,
            2.0f,
            500
    ),
    ELITE(
            3,
            "Elite",
            200_000,
            300,
            2.0f,
            100_000,
            20,
            4.0f,
            2000
    );
    public final int level;
    public final String displayName;

    //Gen
    public final int generatorBuffer;
    public final int generatorOutput;
    public final float generatorEfficiency;

    //Con
    public final int consumerBuffer;
    public final int consumerEnergyPerTick;
    public final float consumerEfficiency;

    //Cable
    public final int cableTransfer;

    MachineTier(int level, String displayName,
                int generatorBuffer, int generatorOutput, float generatorEfficiency,
                int consumerBuffer, int consumerEnergyPerTick, float consumerEfficiency,
                int cableTransfer) {
        this.level = level;
        this.displayName = displayName;
        this.generatorBuffer = generatorBuffer;
        this.generatorOutput = generatorOutput;
        this.generatorEfficiency = generatorEfficiency;
        this.consumerBuffer = consumerBuffer;
        this.consumerEnergyPerTick = consumerEnergyPerTick;
        this.consumerEfficiency = consumerEfficiency;
        this.cableTransfer = cableTransfer;
    }

    //Helper

    public static MachineTier fromId(String id) {
        for (MachineTier tier : MachineTier.values()) {
            if (tier.getId().equals(id)) return tier;
        }
        return BASIC;
    }

    public MachineTier next() {
        return switch (this) {
            case BASIC -> ADVANCED;
            case ADVANCED -> ELITE;
            case ELITE -> null;
        };
    }

    public boolean isMaxTier() {
        return this == ELITE;
    }

    public String getId() {
        return name().toLowerCase();
    }
}
