package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupBuyCategory {
    RECIPE("레시피", "Recipe-based group buy"),
    MEAT("육류", "Beef, Pork, Chicken, Lamb, Goat"),
    SEAFOOD("수산물", "Seafood"),
    VEGETABLE("채소", "Vegetarian, Vegan"),
    GRAIN("곡물/쌀", "Side, Pasta"),
    FRUIT("과일", ""),
    DAIRY("유제품", ""),
    SEASONING("양념/소스", ""),
    SNACK("간식/후식", "Dessert, Starter, Breakfast"),
    ETC("기타", "Miscellaneous");

    private final String displayName;
    private final String description;
}
