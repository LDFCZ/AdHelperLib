package com.example.adhelper;

public enum StartContext {

    SHOP_REWARDED("ShopRewarded"),
    IN_GAME_REWARDED("InGameRewarded"),
    IN_GAME_SAVE_ME("InGameSaveMe"),
    LOOSE("Loose"),
    RANDOM("Random"),
    AFTER_N_GAMES("AfterNGames");

    private String code;

    StartContext(String s) {
        this.code = s;
    }

    public String getCode() {
        return code;
    }
}
