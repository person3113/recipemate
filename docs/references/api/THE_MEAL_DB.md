## Free Recipe API
The API and site will always remain free at point of access.

Test API Keys
You can use the test API key "1" during development of your app or for educational use (see test links below).
However you must become a supporter if releasing publicly on an appstore.

API Production Key Upgrade
All supporters have access to the beta version of the API which allows mutiple ingredient filters.
You also get access to adding your own meals and images. You can also list the full database rather than limited to 100 items.
Please sign up on Paypal and we will email you the upgraded API key.

V1 API
API Methods using the developer test key '1' in the URL

## Search meal by name
www.themealdb.com/api/json/v1/1/search.php?s=Arrabiata
```json
{
  "meals": [
    {
      "idMeal": "52771",
      "strMeal": "Spicy Arrabiata Penne",
      "strMealAlternate": null,
      "strCategory": "Vegetarian",
      "strArea": "Italian",
      "strInstructions": "Bring a large pot of water to a boil. Add kosher salt to the boiling water, then add the pasta. Cook according to the package instructions, about 9 minutes.\r\nIn a large skillet over medium-high heat, add the olive oil and heat until the oil starts to shimmer. Add the garlic and cook, stirring, until fragrant, 1 to 2 minutes. Add the chopped tomatoes, red chile flakes, Italian seasoning and salt and pepper to taste. Bring to a boil and cook for 5 minutes. Remove from the heat and add the chopped basil.\r\nDrain the pasta and add it to the sauce. Garnish with Parmigiano-Reggiano flakes and more basil and serve warm.",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
      "strTags": "Pasta,Curry",
      "strYoutube": "https://www.youtube.com/watch?v=1IszT_guI08",
      "strIngredient1": "penne rigate",
      "strIngredient2": "olive oil",
      "strIngredient3": "garlic",
      "strIngredient4": "chopped tomatoes",
      "strIngredient5": "red chilli flakes",
      "strIngredient6": "italian seasoning",
      "strIngredient7": "basil",
      "strIngredient8": "Parmigiano-Reggiano",
      "strIngredient9": "",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": null,
      "strIngredient17": null,
      "strIngredient18": null,
      "strIngredient19": null,
      "strIngredient20": null,
      "strMeasure1": "1 pound",
      "strMeasure2": "1/4 cup",
      "strMeasure3": "3 cloves",
      "strMeasure4": "1 tin ",
      "strMeasure5": "1/2 teaspoon",
      "strMeasure6": "1/2 teaspoon",
      "strMeasure7": "6 leaves",
      "strMeasure8": "sprinkling",
      "strMeasure9": "",
      "strMeasure10": "",
      "strMeasure11": "",
      "strMeasure12": "",
      "strMeasure13": "",
      "strMeasure14": "",
      "strMeasure15": "",
      "strMeasure16": null,
      "strMeasure17": null,
      "strMeasure18": null,
      "strMeasure19": null,
      "strMeasure20": null,
      "strSource": null,
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": null
    }
  ]
}
```

## List all meals by first letter
www.themealdb.com/api/json/v1/1/search.php?f=a
```json
{
  "meals": [
    {
      "idMeal": "53111",
      "strMeal": "Anzac biscuits",
      "strMealAlternate": null,
      "strCategory": "Dessert",
      "strArea": "Australian",
      "strInstructions": "step 1\r\nHeat oven to 180C/fan 160C/gas 4. Put the oats, coconut, flour and sugar in a bowl. Melt the butter in a small pan and stir in the golden syrup. Add the bicarbonate of soda to 2 tbsp boiling water, then stir into the golden syrup and butter mixture.\r\n\r\nstep 2\r\nMake a well in the middle of the dry ingredients and pour in the butter and golden syrup mixture. Stir gently to incorporate the dry ingredients.\r\n\r\nstep 3\r\nPut dessertspoonfuls of the mixture on to buttered baking sheets, about 2.5cm/1in apart to allow room for spreading. Bake in batches for 8-10 mins until golden. Transfer to a wire rack to cool.",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/q47rkb1762324620.jpg",
      "strTags": null,
      "strYoutube": "https://www.youtube.com/watch?v=1nAVbfQVWRQ",
      "strIngredient1": "Porridge oats",
      "strIngredient2": "Desiccated Coconut",
      "strIngredient3": "Plain Flour",
      "strIngredient4": "Caster Sugar",
      "strIngredient5": "Butter",
      "strIngredient6": "Golden Syrup",
      "strIngredient7": "Bicarbonate Of Soda",
      "strIngredient8": "",
      "strIngredient9": "",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": "",
      "strIngredient17": "",
      "strIngredient18": "",
      "strIngredient19": "",
      "strIngredient20": "",
      "strMeasure1": "85g",
      "strMeasure2": "85g",
      "strMeasure3": "100g ",
      "strMeasure4": "100g ",
      "strMeasure5": "100g ",
      "strMeasure6": "1 tblsp ",
      "strMeasure7": "1 teaspoon",
      "strMeasure8": " ",
      "strMeasure9": " ",
      "strMeasure10": " ",
      "strMeasure11": " ",
      "strMeasure12": " ",
      "strMeasure13": " ",
      "strMeasure14": " ",
      "strMeasure15": " ",
      "strMeasure16": " ",
      "strMeasure17": " ",
      "strMeasure18": " ",
      "strMeasure19": " ",
      "strMeasure20": " ",
      "strSource": "https://www.bbcgoodfood.com/recipes/anzac-biscuits",
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": "2025-11-05 04:46:03"
    },
    {
      "idMeal": "53049",
      "strMeal": "Apam balik",
      "strMealAlternate": null,
      "strCategory": "Dessert",
      "strArea": "Malaysian",
      "strInstructions": "Mix milk, oil and egg together. Sift flour, baking powder and salt into the mixture. Stir well until all ingredients are combined evenly.\r\n\r\nSpread some batter onto the pan. Spread a thin layer of batter to the side of the pan. Cover the pan for 30-60 seconds until small air bubbles appear.\r\n\r\nAdd butter, cream corn, crushed peanuts and sugar onto the pancake. Fold the pancake into half once the bottom surface is browned.\r\n\r\nCut into wedges and best eaten when it is warm.",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/adxcbq1619787919.jpg",
      "strTags": null,
      "strYoutube": "https://www.youtube.com/watch?v=6R8ffRRJcrg",
      "strIngredient1": "Milk",
      "strIngredient2": "Oil",
      "strIngredient3": "Eggs",
      "strIngredient4": "Flour",
      "strIngredient5": "Baking Powder",
      "strIngredient6": "Salt",
      "strIngredient7": "Unsalted Butter",
      "strIngredient8": "Sugar",
      "strIngredient9": "Peanut Butter",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": "",
      "strIngredient17": "",
      "strIngredient18": "",
      "strIngredient19": "",
      "strIngredient20": "",
      "strMeasure1": "200ml",
      "strMeasure2": "60ml",
      "strMeasure3": "2",
      "strMeasure4": "1600g",
      "strMeasure5": "3 tsp",
      "strMeasure6": "1/2 tsp",
      "strMeasure7": "25g",
      "strMeasure8": "45g",
      "strMeasure9": "3 tbs",
      "strMeasure10": " ",
      "strMeasure11": " ",
      "strMeasure12": " ",
      "strMeasure13": " ",
      "strMeasure14": " ",
      "strMeasure15": " ",
      "strMeasure16": " ",
      "strMeasure17": " ",
      "strMeasure18": " ",
      "strMeasure19": " ",
      "strMeasure20": " ",
      "strSource": "https://www.nyonyacooking.com/recipes/apam-balik~SJ5WuvsDf9WQ",
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": null
    },
    {
      "idMeal": "52893",
      "strMeal": "Apple & Blackberry Crumble",
      "strMealAlternate": null,
      "strCategory": "Dessert",
      "strArea": "British",
      "strInstructions": "Heat oven to 190C/170C fan/gas 5. Tip the flour and sugar into a large bowl. Add the butter, then rub into the flour using your fingertips to make a light breadcrumb texture. Do not overwork it or the crumble will become heavy. Sprinkle the mixture evenly over a baking sheet and bake for 15 mins or until lightly coloured.\r\nMeanwhile, for the compote, peel, core and cut the apples into 2cm dice. Put the butter and sugar in a medium saucepan and melt together over a medium heat. Cook for 3 mins until the mixture turns to a light caramel. Stir in the apples and cook for 3 mins. Add the blackberries and cinnamon, and cook for 3 mins more. Cover, remove from the heat, then leave for 2-3 mins to continue cooking in the warmth of the pan.\r\nTo serve, spoon the warm fruit into an ovenproof gratin dish, top with the crumble mix, then reheat in the oven for 5-10 mins. Serve with vanilla ice cream.",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/xvsurr1511719182.jpg",
      "strTags": "Pudding",
      "strYoutube": "https://www.youtube.com/watch?v=4vhcOwVBDO4",
      "strIngredient1": "Plain Flour",
      "strIngredient2": "Caster Sugar",
      "strIngredient3": "Butter",
      "strIngredient4": "Braeburn Apples",
      "strIngredient5": "Butter",
      "strIngredient6": "Demerara Sugar",
      "strIngredient7": "Blackberries",
      "strIngredient8": "Cinnamon",
      "strIngredient9": "Ice Cream",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": "",
      "strIngredient17": "",
      "strIngredient18": "",
      "strIngredient19": "",
      "strIngredient20": "",
      "strMeasure1": "120g",
      "strMeasure2": "60g",
      "strMeasure3": "60g",
      "strMeasure4": "300g",
      "strMeasure5": "30g",
      "strMeasure6": "30g",
      "strMeasure7": "120g",
      "strMeasure8": "¼ teaspoon",
      "strMeasure9": "to serve",
      "strMeasure10": "",
      "strMeasure11": "",
      "strMeasure12": "",
      "strMeasure13": "",
      "strMeasure14": "",
      "strMeasure15": "",
      "strMeasure16": "",
      "strMeasure17": "",
      "strMeasure18": "",
      "strMeasure19": "",
      "strMeasure20": "",
      "strSource": "https://www.bbcgoodfood.com/recipes/778642/apple-and-blackberry-crumble",
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": null
    },
    {
      "idMeal": "52768",
      "strMeal": "Apple Frangipan Tart",
      "strMealAlternate": null,
      "strCategory": "Dessert",
      "strArea": "British",
      "strInstructions": "Preheat the oven to 200C/180C Fan/Gas 6.\r\nPut the biscuits in a large re-sealable freezer bag and bash with a rolling pin into fine crumbs. Melt the butter in a small pan, then add the biscuit crumbs and stir until coated with butter. Tip into the tart tin and, using the back of a spoon, press over the base and sides of the tin to give an even layer. Chill in the fridge while you make the filling.\r\nCream together the butter and sugar until light and fluffy. You can do this in a food processor if you have one. Process for 2-3 minutes. Mix in the eggs, then add the ground almonds and almond extract and blend until well combined.\r\nPeel the apples, and cut thin slices of apple. Do this at the last minute to prevent the apple going brown. Arrange the slices over the biscuit base. Spread the frangipane filling evenly on top. Level the surface and sprinkle with the flaked almonds.\r\nBake for 20-25 minutes until golden-brown and set.\r\nRemove from the oven and leave to cool for 15 minutes. Remove the sides of the tin. An easy way to do this is to stand the tin on a can of beans and push down gently on the edges of the tin.\r\nTransfer the tart, with the tin base attached, to a serving plate. Serve warm with cream, crème fraiche or ice cream.",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wxywrq1468235067.jpg",
      "strTags": "Tart,Baking,Fruity",
      "strYoutube": "https://www.youtube.com/watch?v=rp8Slv4INLk",
      "strIngredient1": "digestive biscuits",
      "strIngredient2": "butter",
      "strIngredient3": "Bramley apples",
      "strIngredient4": "Salted Butter",
      "strIngredient5": "caster sugar",
      "strIngredient6": "free-range eggs, beaten",
      "strIngredient7": "ground almonds",
      "strIngredient8": "almond extract",
      "strIngredient9": "flaked almonds",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": null,
      "strIngredient17": null,
      "strIngredient18": null,
      "strIngredient19": null,
      "strIngredient20": null,
      "strMeasure1": "175g/6oz",
      "strMeasure2": "75g/3oz",
      "strMeasure3": "200g/7oz",
      "strMeasure4": "75g/3oz",
      "strMeasure5": "75g/3oz",
      "strMeasure6": "2",
      "strMeasure7": "75g/3oz",
      "strMeasure8": "1 tsp",
      "strMeasure9": "50g/1¾oz",
      "strMeasure10": "",
      "strMeasure11": "",
      "strMeasure12": "",
      "strMeasure13": "",
      "strMeasure14": "",
      "strMeasure15": "",
      "strMeasure16": null,
      "strMeasure17": null,
      "strMeasure18": null,
      "strMeasure19": null,
      "strMeasure20": null,
      "strSource": null,
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": null
    },
    {
      "idMeal": "53099",
      "strMeal": "Aussie Burgers",
      "strMealAlternate": null,
      "strCategory": "Beef",
      "strArea": "Australian",
      "strInstructions": "step 1\r\nMake the burgers: Tip the meat into a bowl and sprinkle over 1 tsp salt and a good grinding of black pepper.Work with wet hands to mix in the seasoning. Divide into four with your hands and shape into burgers. (It can be frozen at this stage.)\r\n\r\nstep 2\r\nSort out your ingredients: Slice the beetroot and split the naan breads.\r\n\r\nstep 3\r\nToast the naans: Heat a griddle pan or barbecue. Griddle the naans on both sides until lightly toasted and set aside. Add the burgers to the grill or barbecue and cook for 2-3 minutes, then turn and cook the other side for a further 2-3 minutes.\r\n\r\nstep 4\r\nAssemble the dish: Set half a toasted naan on each serving plate and put a pile of rocket on each. Top with a burger, then a few slices of beetroot and a dollop of soured cream. Sprinkle with salt and freshly ground black pepper and serve immediately with a big green salad and chips. A glass of red wine wouldn’t go amiss, either.",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/44bzep1761848278.jpg",
      "strTags": null,
      "strYoutube": "https://www.youtube.com/watch?v=pSupybydA_U",
      "strIngredient1": "Lean Minced Steak",
      "strIngredient2": "Cooked Beetroot",
      "strIngredient3": "Naan Bread",
      "strIngredient4": "Rocket",
      "strIngredient5": "Soured cream and chive dip",
      "strIngredient6": "",
      "strIngredient7": "",
      "strIngredient8": "",
      "strIngredient9": "",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": "",
      "strIngredient17": "",
      "strIngredient18": "",
      "strIngredient19": "",
      "strIngredient20": "",
      "strMeasure1": "500g",
      "strMeasure2": "100g ",
      "strMeasure3": "2 small",
      "strMeasure4": "50g",
      "strMeasure5": "4 tablespoons",
      "strMeasure6": " ",
      "strMeasure7": " ",
      "strMeasure8": " ",
      "strMeasure9": " ",
      "strMeasure10": " ",
      "strMeasure11": " ",
      "strMeasure12": " ",
      "strMeasure13": " ",
      "strMeasure14": " ",
      "strMeasure15": " ",
      "strMeasure16": " ",
      "strMeasure17": " ",
      "strMeasure18": " ",
      "strMeasure19": " ",
      "strMeasure20": " ",
      "strSource": "https://www.bbcgoodfood.com/recipes/aussie-burgers",
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": "2025-10-30 15:03:15"
    },
    {
      "idMeal": "53107",
      "strMeal": "Avocado dip with new potatoes",
      "strMealAlternate": null,
      "strCategory": "Vegetarian",
      "strArea": "Australian",
      "strInstructions": "step 1\r\nWhizz half the avocado flesh with the yogurt, lime and lemon juice and seasoning. Dice the remaining avocado, then gently stir into the whizzed mix with most of the lime zest. Cover, then chill until ready to serve.\r\n\r\nstep 2\r\nBoil potatoes for 6 mins, then drain well and toss with olive oil, chilli powder and cumin seeds. Now set aside until half an hour before your guests arrive.\r\n\r\nstep 3\r\nHeat oven to 200C/180C fan/gas 6, then roast potatoes for about 30 mins, shaking the tray halfway, until golden and tender. Transfer the dip to one or two bowls, scatter with the remaining lime zest and serve with the hot potatoes, and tortilla chips for dipping.",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/flrajf1762341295.jpg",
      "strTags": null,
      "strYoutube": "",
      "strIngredient1": "Avocado",
      "strIngredient2": "Natural Yoghurt",
      "strIngredient3": "Lime",
      "strIngredient4": "Lemon",
      "strIngredient5": "Baby New Potatoes",
      "strIngredient6": "Olive Oil",
      "strIngredient7": "Hot Chilli Powder",
      "strIngredient8": "Cumin Seeds",
      "strIngredient9": "Tortillas",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": "",
      "strIngredient17": "",
      "strIngredient18": "",
      "strIngredient19": "",
      "strIngredient20": "",
      "strMeasure1": "3 Large",
      "strMeasure2": "200g",
      "strMeasure3": "Zest and juice of 1",
      "strMeasure4": "Juice of 1/2",
      "strMeasure5": "1.25kg",
      "strMeasure6": "2 tablespoons",
      "strMeasure7": "1 teaspoon",
      "strMeasure8": "1 teaspoon",
      "strMeasure9": "200g",
      "strMeasure10": " ",
      "strMeasure11": " ",
      "strMeasure12": " ",
      "strMeasure13": " ",
      "strMeasure14": " ",
      "strMeasure15": " ",
      "strMeasure16": " ",
      "strMeasure17": " ",
      "strMeasure18": " ",
      "strMeasure19": " ",
      "strMeasure20": " ",
      "strSource": "https://www.bbcgoodfood.com/recipes/avocado-citrus-dip-spicy-spuds-tortilla-chips",
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": "2025-11-05 02:37:50"
    },
    {
      "idMeal": "53050",
      "strMeal": "Ayam Percik",
      "strMealAlternate": null,
      "strCategory": "Chicken",
      "strArea": "Malaysian",
      "strInstructions": "In a blender, add the ingredients for the spice paste and blend until smooth.\r\nOver medium heat, pour the spice paste in a skillet or pan and fry for 10 minutes until fragrant. Add water or oil 1 tablespoon at a time if the paste becomes too dry. Don't burn the paste. Lower the fire slightly if needed.\r\nAdd the cloves, cardamom, tamarind pulp, coconut milk, water, sugar and salt. Turn the heat up and bring the mixture to boil. Turn the heat to medium low and simmer for 10 minutes. Stir occasionally. It will reduce slightly. This is the marinade/sauce, so taste and adjust seasoning if necessary. Don't worry if it's slightly bitter. It will go away when roasting.\r\nWhen the marinade/sauce has cooled, pour everything over the chicken and marinate overnight to two days.\r\nPreheat the oven to 425 F.\r\nRemove the chicken from the marinade. Spoon the marinade onto a greased (or aluminum lined) baking sheet. Lay the chicken on top of the sauce (make sure the chicken covers the sauce and the sauce isn't exposed or it'll burn) and spread the remaining marinade on the chicken. Roast for 35-45 minutes or until internal temp of the thickest part of chicken is at least 175 F.\r\nLet chicken rest for 5 minutes. Brush the chicken with some of the oil. Serve chicken with the sauce over steamed rice (or coconut rice).",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/020z181619788503.jpg",
      "strTags": null,
      "strYoutube": "https://www.youtube.com/watch?v=9ytR28QK6I8",
      "strIngredient1": "Chicken Thighs",
      "strIngredient2": "Challots",
      "strIngredient3": "Ginger",
      "strIngredient4": "Garlic Clove",
      "strIngredient5": "Cayenne Pepper",
      "strIngredient6": "Turmeric",
      "strIngredient7": "Cumin",
      "strIngredient8": "Coriander",
      "strIngredient9": "Fennel",
      "strIngredient10": "Tamarind Paste",
      "strIngredient11": "Coconut Milk",
      "strIngredient12": "Sugar",
      "strIngredient13": "Water",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": "",
      "strIngredient17": "",
      "strIngredient18": "",
      "strIngredient19": "",
      "strIngredient20": "",
      "strMeasure1": "6",
      "strMeasure2": "16",
      "strMeasure3": "1 1/2 ",
      "strMeasure4": "6",
      "strMeasure5": "8",
      "strMeasure6": "2 tbs",
      "strMeasure7": "1 1/2 ",
      "strMeasure8": "1 1/2 ",
      "strMeasure9": "1 1/2 ",
      "strMeasure10": "2 tbs",
      "strMeasure11": "1 can ",
      "strMeasure12": "1 tsp ",
      "strMeasure13": "1 cup ",
      "strMeasure14": " ",
      "strMeasure15": " ",
      "strMeasure16": " ",
      "strMeasure17": " ",
      "strMeasure18": " ",
      "strMeasure19": " ",
      "strMeasure20": " ",
      "strSource": "http://www.curiousnut.com/roasted-spiced-chicken-ayam-percik/",
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": null
    }
  ]
}
```

## Lookup full meal details by id
www.themealdb.com/api/json/v1/1/lookup.php?i=52772
```json
{
  "meals": [
    {
      "idMeal": "52772",
      "strMeal": "Teriyaki Chicken Casserole",
      "strMealAlternate": null,
      "strCategory": "Chicken",
      "strArea": "Japanese",
      "strInstructions": "Preheat oven to 350° F. Spray a 9x13-inch baking pan with non-stick spray.\r\nCombine soy sauce, ½ cup water, brown sugar, ginger and garlic in a small saucepan and cover. Bring to a boil over medium heat. Remove lid and cook for one minute once boiling.\r\nMeanwhile, stir together the corn starch and 2 tablespoons of water in a separate dish until smooth. Once sauce is boiling, add mixture to the saucepan and stir to combine. Cook until the sauce starts to thicken then remove from heat.\r\nPlace the chicken breasts in the prepared pan. Pour one cup of the sauce over top of chicken. Place chicken in oven and bake 35 minutes or until cooked through. Remove from oven and shred chicken in the dish using two forks.\r\n*Meanwhile, steam or cook the vegetables according to package directions.\r\nAdd the cooked vegetables and rice to the casserole dish with the chicken. Add most of the remaining sauce, reserving a bit to drizzle over the top when serving. Gently toss everything together in the casserole dish until combined. Return to oven and cook 15 minutes. Remove from oven and let stand 5 minutes before serving. Drizzle each serving with remaining sauce. Enjoy!",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
      "strTags": "Meat,Casserole",
      "strYoutube": "https://www.youtube.com/watch?v=4aZr5hZXP_s",
      "strIngredient1": "soy sauce",
      "strIngredient2": "water",
      "strIngredient3": "brown sugar",
      "strIngredient4": "ground ginger",
      "strIngredient5": "minced garlic",
      "strIngredient6": "cornstarch",
      "strIngredient7": "chicken breasts",
      "strIngredient8": "stir-fry vegetables",
      "strIngredient9": "brown rice",
      "strIngredient10": "",
      "strIngredient11": "",
      "strIngredient12": "",
      "strIngredient13": "",
      "strIngredient14": "",
      "strIngredient15": "",
      "strIngredient16": null,
      "strIngredient17": null,
      "strIngredient18": null,
      "strIngredient19": null,
      "strIngredient20": null,
      "strMeasure1": "3/4 cup",
      "strMeasure2": "1/2 cup",
      "strMeasure3": "1/4 cup",
      "strMeasure4": "1/2 teaspoon",
      "strMeasure5": "1/2 teaspoon",
      "strMeasure6": "4 Tablespoons",
      "strMeasure7": "2",
      "strMeasure8": "1 (12 oz.)",
      "strMeasure9": "3 cups",
      "strMeasure10": "",
      "strMeasure11": "",
      "strMeasure12": "",
      "strMeasure13": "",
      "strMeasure14": "",
      "strMeasure15": "",
      "strMeasure16": null,
      "strMeasure17": null,
      "strMeasure18": null,
      "strMeasure19": null,
      "strMeasure20": null,
      "strSource": null,
      "strImageSource": null,
      "strCreativeCommonsConfirmed": null,
      "dateModified": null
    }
  ]
}
```
## Lookup a single random meal
www.themealdb.com/api/json/v1/1/random.php

## List all meal categories
www.themealdb.com/api/json/v1/1/categories.php
```json
{
  "categories": [
    {
      "idCategory": "1",
      "strCategory": "Beef",
      "strCategoryThumb": "https://www.themealdb.com/images/category/beef.png",
      "strCategoryDescription": "Beef is the culinary name for meat from cattle, particularly skeletal muscle. Humans have been eating beef since prehistoric times.[1] Beef is a source of high-quality protein and essential nutrients.[2]"
    },
    {
      "idCategory": "2",
      "strCategory": "Chicken",
      "strCategoryThumb": "https://www.themealdb.com/images/category/chicken.png",
      "strCategoryDescription": "Chicken is a type of domesticated fowl, a subspecies of the red junglefowl. It is one of the most common and widespread domestic animals, with a total population of more than 19 billion as of 2011.[1] Humans commonly keep chickens as a source of food (consuming both their meat and eggs) and, more rarely, as pets."
    },
    {
      "idCategory": "3",
      "strCategory": "Dessert",
      "strCategoryThumb": "https://www.themealdb.com/images/category/dessert.png",
      "strCategoryDescription": "Dessert is a course that concludes a meal. The course usually consists of sweet foods, such as confections dishes or fruit, and possibly a beverage such as dessert wine or liqueur, however in the United States it may include coffee, cheeses, nuts, or other savory items regarded as a separate course elsewhere. In some parts of the world, such as much of central and western Africa, and most parts of China, there is no tradition of a dessert course to conclude a meal.\r\n\r\nThe term dessert can apply to many confections, such as biscuits, cakes, cookies, custards, gelatins, ice creams, pastries, pies, puddings, and sweet soups, and tarts. Fruit is also commonly found in dessert courses because of its naturally occurring sweetness. Some cultures sweeten foods that are more commonly savory to create desserts."
    },
    {
      "idCategory": "4",
      "strCategory": "Lamb",
      "strCategoryThumb": "https://www.themealdb.com/images/category/lamb.png",
      "strCategoryDescription": "Lamb, hogget, and mutton are the meat of domestic sheep (species Ovis aries) at different ages.\r\n\r\nA sheep in its first year is called a lamb, and its meat is also called lamb. The meat of a juvenile sheep older than one year is hogget; outside the USA this is also a term for the living animal. The meat of an adult sheep is mutton, a term only used for the meat, not the living animals. The term mutton is almost always used to refer to goat meat in the Indian subcontinent.\r\n\r\n"
    },
    {
      "idCategory": "5",
      "strCategory": "Miscellaneous",
      "strCategoryThumb": "https://www.themealdb.com/images/category/miscellaneous.png",
      "strCategoryDescription": "General foods that don't fit into another category"
    },
    {
      "idCategory": "6",
      "strCategory": "Pasta",
      "strCategoryThumb": "https://www.themealdb.com/images/category/pasta.png",
      "strCategoryDescription": "Pasta is a staple food of traditional Italian cuisine, with the first reference dating to 1154 in Sicily.\r\n\r\nAlso commonly used to refer to the variety of pasta dishes, pasta is typically a noodle made from an unleavened dough of a durum wheat flour mixed with water or eggs and formed into sheets or various shapes, then cooked by boiling or baking. As an alternative for those wanting a different taste, or who need to avoid products containing gluten, some pastas can be made using rice flour in place of wheat.[3][4] Pastas may be divided into two broad categories, dried (pasta secca) and fresh (pasta fresca)."
    },
    {
      "idCategory": "7",
      "strCategory": "Pork",
      "strCategoryThumb": "https://www.themealdb.com/images/category/pork.png",
      "strCategoryDescription": "Pork is the culinary name for meat from a domestic pig (Sus scrofa domesticus). It is the most commonly consumed meat worldwide,[1] with evidence of pig husbandry dating back to 5000 BC. Pork is eaten both freshly cooked and preserved. Curing extends the shelf life of the pork products. Ham, smoked pork, gammon, bacon and sausage are examples of preserved pork. Charcuterie is the branch of cooking devoted to prepared meat products, many from pork.\r\n\r\nPork is the most popular meat in Eastern and Southeastern Asia, and is also very common in the Western world, especially in Central Europe. It is highly prized in Asian cuisines for its fat content and pleasant texture. Consumption of pork is forbidden by Jewish and Muslim dietary law, a taboo that is deeply rooted in tradition, with several suggested possible causes. The sale of pork is limited in Israel and illegal in certain Muslim countries."
    },
    {
      "idCategory": "8",
      "strCategory": "Seafood",
      "strCategoryThumb": "https://www.themealdb.com/images/category/seafood.png",
      "strCategoryDescription": "Seafood is any form of sea life regarded as food by humans. Seafood prominently includes fish and shellfish. Shellfish include various species of molluscs, crustaceans, and echinoderms. Historically, sea mammals such as whales and dolphins have been consumed as food, though that happens to a lesser extent in modern times. Edible sea plants, such as some seaweeds and microalgae, are widely eaten as seafood around the world, especially in Asia (see the category of sea vegetables). In North America, although not generally in the United Kingdom, the term \"seafood\" is extended to fresh water organisms eaten by humans, so all edible aquatic life may be referred to as seafood. For the sake of completeness, this article includes all edible aquatic life."
    },
    {
      "idCategory": "9",
      "strCategory": "Side",
      "strCategoryThumb": "https://www.themealdb.com/images/category/side.png",
      "strCategoryDescription": "A side dish, sometimes referred to as a side order, side item, or simply a side, is a food item that accompanies the entrée or main course at a meal. Side dishes such as salad, potatoes and bread are commonly used with main courses throughout many countries of the western world. New side orders introduced within the past decade[citation needed], such as rice and couscous, have grown to be quite popular throughout Europe, especially at formal occasions (with couscous appearing more commonly at dinner parties with Middle Eastern dishes)."
    },
    {
      "idCategory": "10",
      "strCategory": "Starter",
      "strCategoryThumb": "https://www.themealdb.com/images/category/starter.png",
      "strCategoryDescription": "An entrée in modern French table service and that of much of the English-speaking world (apart from the United States and parts of Canada) is a dish served before the main course of a meal; it may be the first dish served, or it may follow a soup or other small dish or dishes. In the United States and parts of Canada, an entrée is the main dish or the only dish of a meal.\r\n\r\nHistorically, the entrée was one of the stages of the “Classical Order” of formal French table service of the 18th and 19th centuries. It formed a part of the “first service” of the meal, which consisted of potage, hors d’œuvre, and entrée (including the bouilli and relevé). The “second service” consisted of roast (rôti), salad, and entremets (the entremets sometimes being separated into a “third service” of their own). The final service consisted only of dessert.[3]:3–11 :13–25"
    },
    {
      "idCategory": "11",
      "strCategory": "Vegan",
      "strCategoryThumb": "https://www.themealdb.com/images/category/vegan.png",
      "strCategoryDescription": "Veganism is both the practice of abstaining from the use of animal products, particularly in diet, and an associated philosophy that rejects the commodity status of animals.[b] A follower of either the diet or the philosophy is known as a vegan (pronounced /ˈviːɡən/ VEE-gən). Distinctions are sometimes made between several categories of veganism. Dietary vegans (or strict vegetarians) refrain from consuming animal products, not only meat but also eggs, dairy products and other animal-derived substances.[c] The term ethical vegan is often applied to those who not only follow a vegan diet but extend the philosophy into other areas of their lives, and oppose the use of animals for any purpose.[d] Another term is environmental veganism, which refers to the avoidance of animal products on the premise that the harvesting or industrial farming of animals is environmentally damaging and unsustainable."
    },
    {
      "idCategory": "12",
      "strCategory": "Vegetarian",
      "strCategoryThumb": "https://www.themealdb.com/images/category/vegetarian.png",
      "strCategoryDescription": "Vegetarianism is the practice of abstaining from the consumption of meat (red meat, poultry, seafood, and the flesh of any other animal), and may also include abstention from by-products of animal slaughter.\r\n\r\nVegetarianism may be adopted for various reasons. Many people object to eating meat out of respect for sentient life. Such ethical motivations have been codified under various religious beliefs, as well as animal rights advocacy. Other motivations for vegetarianism are health-related, political, environmental, cultural, aesthetic, economic, or personal preference. There are variations of the diet as well: an ovo-lacto vegetarian diet includes both eggs and dairy products, an ovo-vegetarian diet includes eggs but not dairy products, and a lacto-vegetarian diet includes dairy products but not eggs. A vegan diet excludes all animal products, including eggs and dairy. Some vegans also avoid other animal products such as beeswax, leather or silk clothing, and goose-fat shoe polish."
    },
    {
      "idCategory": "13",
      "strCategory": "Breakfast",
      "strCategoryThumb": "https://www.themealdb.com/images/category/breakfast.png",
      "strCategoryDescription": "Breakfast is the first meal of a day. The word in English refers to breaking the fasting period of the previous night. There is a strong likelihood for one or more \"typical\", or \"traditional\", breakfast menus to exist in most places, but their composition varies widely from place to place, and has varied over time, so that globally a very wide range of preparations and ingredients are now associated with breakfast."
    },
    {
      "idCategory": "14",
      "strCategory": "Goat",
      "strCategoryThumb": "https://www.themealdb.com/images/category/goat.png",
      "strCategoryDescription": "The domestic goat or simply goat (Capra aegagrus hircus) is a subspecies of C. aegagrus domesticated from the wild goat of Southwest Asia and Eastern Europe. The goat is a member of the animal family Bovidae and the subfamily Caprinae, meaning it is closely related to the sheep. There are over 300 distinct breeds of goat. Goats are one of the oldest domesticated species of animal, and have been used for milk, meat, fur and skins across much of the world. Milk from goats is often turned into goat cheese."
    }
  ]
}
```

## List all Categories, Area, Ingredients
www.themealdb.com/api/json/v1/1/list.php?c=list
```json
{
  "meals": [
    {
      "strCategory": "Beef"
    },
    {
      "strCategory": "Breakfast"
    },
    {
      "strCategory": "Chicken"
    },
    {
      "strCategory": "Dessert"
    },
    {
      "strCategory": "Goat"
    },
    {
      "strCategory": "Lamb"
    },
    {
      "strCategory": "Miscellaneous"
    },
    {
      "strCategory": "Pasta"
    },
    {
      "strCategory": "Pork"
    },
    {
      "strCategory": "Seafood"
    },
    {
      "strCategory": "Side"
    },
    {
      "strCategory": "Starter"
    },
    {
      "strCategory": "Vegan"
    },
    {
      "strCategory": "Vegetarian"
    }
  ]
}
```

www.themealdb.com/api/json/v1/1/list.php?a=list
```json
{
  "meals": [
    {
      "strArea": "American"
    },
    {
      "strArea": "Australian"
    },
    {
      "strArea": "British"
    },
    {
      "strArea": "Canadian"
    },
    {
      "strArea": "Chinese"
    },
    {
      "strArea": "Croatian"
    },
    {
      "strArea": "Dutch"
    },
    {
      "strArea": "Egyptian"
    },
    {
      "strArea": "Filipino"
    },
    {
      "strArea": "French"
    },
    {
      "strArea": "Greek"
    },
    {
      "strArea": "Indian"
    },
    {
      "strArea": "Irish"
    },
    {
      "strArea": "Italian"
    },
    {
      "strArea": "Jamaican"
    },
    {
      "strArea": "Japanese"
    },
    {
      "strArea": "Kenyan"
    },
    {
      "strArea": "Malaysian"
    },
    {
      "strArea": "Mexican"
    },
    {
      "strArea": "Moroccan"
    },
    {
      "strArea": "Norwegian"
    },
    {
      "strArea": "Polish"
    },
    {
      "strArea": "Portuguese"
    },
    {
      "strArea": "Russian"
    },
    {
      "strArea": "Spanish"
    },
    {
      "strArea": "Syrian"
    },
    {
      "strArea": "Thai"
    },
    {
      "strArea": "Tunisian"
    },
    {
      "strArea": "Turkish"
    },
    {
      "strArea": "Ukrainian"
    },
    {
      "strArea": "Uruguayan"
    },
    {
      "strArea": "Vietnamese"
    }
  ]
}
```

www.themealdb.com/api/json/v1/1/list.php?i=list
json 응답 길어서 일부만 나열
```json
{
  "meals": [
    {
      "idIngredient": "1",
      "strIngredient": "Chicken",
      "strDescription": "The chicken is a type of domesticated fowl, a subspecies of the red junglefowl (Gallus gallus). It is one of the most common and widespread domestic animals, with a total population of more than 19 billion as of 2011. There are more chickens in the world than any other bird or domesticated fowl. Humans keep chickens primarily as a source of food (consuming both their meat and eggs) and, less commonly, as pets. Originally raised for cockfighting or for special ceremonies, chickens were not kept for food until the Hellenistic period (4th–2nd centuries BC).\r\n\r\nGenetic studies have pointed to multiple maternal origins in South Asia, Southeast Asia, and East Asia, but with the clade found in the Americas, Europe, the Middle East and Africa originating in the Indian subcontinent. From ancient India, the domesticated chicken spread to Lydia in western Asia Minor, and to Greece by the 5th century BC. Fowl had been known in Egypt since the mid-15th century BC, with the \"bird that gives birth every day\" having come to Egypt from the land between Syria and Shinar, Babylonia, according to the annals of Thutmose III.",
      "strThumb": "https://www.themealdb.com/ingredients/chicken.png",
      "strType": null
    },
    {
      "idIngredient": "2",
      "strIngredient": "Salmon",
      "strDescription": "Salmon is the common name for several species of ray-finned fish in the family Salmonidae. Other fish in the same family include trout, char, grayling and whitefish. Salmon are native to tributaries of the North Atlantic (genus Salmo) and Pacific Ocean (genus Oncorhynchus). Many species of salmon have been introduced into non-native environments such as the Great Lakes of North America and Patagonia in South America. Salmon are intensively farmed in many parts of the world.\r\n\r\nTypically, salmon are anadromous: they hatch in fresh water, migrate to the ocean, then return to fresh water to reproduce. However, populations of several species are restricted to fresh water through their lives. Folklore has it that the fish return to the exact spot where they hatched to spawn. Tracking studies have shown this to be mostly true. A portion of a returning salmon run may stray and spawn in different freshwater systems; the percent of straying depends on the species of salmon. Homing behavior has been shown to depend on olfactory memory. Salmon date back to the Neogene.",
      "strThumb": "https://www.themealdb.com/images/ingredients/salmon.png",
      "strType": null
    },
    {
      "idIngredient": "3",
      "strIngredient": "Beef",
      "strDescription": "Beef is the culinary name for meat from cattle, particularly skeletal muscle. Humans have been eating beef since prehistoric times. Beef is a source of high-quality protein and nutrients.\r\n\r\nMost beef skeletal muscle meat can be used as is by merely cutting into certain parts, such as roasts, short ribs or steak (filet mignon, sirloin steak, rump steak, rib steak, rib eye steak, hanger steak, etc.), while other cuts are processed (corned beef or beef jerky). Trimmings, on the other hand, are usually mixed with meat from older, leaner (therefore tougher) cattle, are ground, minced or used in sausages. The blood is used in some varieties called blood sausage. Other parts that are eaten include other muscles and offal, such as the oxtail, liver, tongue, tripe from the reticulum or rumen, glands (particularly the pancreas and thymus, referred to as sweetbread), the heart, the brain (although forbidden where there is a danger of bovine spongiform encephalopathy, BSE, commonly referred to as mad cow disease), the kidneys, and the tender testicles of the bull (known in the United States as calf fries, prairie oysters, or Rocky Mountain oysters). Some intestines are cooked and eaten as is, but are more often cleaned and used as natural sausage casings. The bones are used for making beef stock.",
      "strThumb": "https://www.themealdb.com/images/ingredients/beef.png",
      "strType": null
    },
    {
      "idIngredient": "4",
      "strIngredient": "Pork",
      "strDescription": "Pork is the culinary name for the flesh of a domestic pig (Sus scrofa domesticus). It is the most commonly consumed meat worldwide,[1] with evidence of pig husbandry dating back to 5000 BC.\r\n\r\nPork is eaten both freshly cooked and preserved. Curing extends the shelf life of the pork products. Ham, smoked pork, gammon, bacon and sausage are examples of preserved pork. Charcuterie is the branch of cooking devoted to prepared meat products, many from pork.\r\n\r\nPig is the most popular meat in the Eastern and non-Muslim parts of Southeastern Asia (Indochina, Philippines, Singapore, East Timor) and is also very common in the Western world, especially in Central Europe. It is highly prized in Asian cuisines for its fat content and pleasant texture. Consumption of pork is forbidden by Jewish, Muslim and Rastafarian dietary law, for religious reasons, with several suggested possible causes.",
      "strThumb": "https://www.themealdb.com/images/ingredients/pork.png",
      "strType": null
    },
    {
      "idIngredient": "5",
      "strIngredient": "Avocado",
      "strDescription": "The avocado, a tree with probable origin in South Central Mexico, is classified as a member of the flowering plant family Lauraceae. The fruit of the plant, also called an avocado (or avocado pear or alligator pear), is botanically a large berry containing a single large seed.\r\n\r\nAvocados are commercially valuable and are cultivated in tropical and Mediterranean climates throughout the world. They have a green-skinned, fleshy body that may be pear-shaped, egg-shaped, or spherical. Commercially, they ripen after harvesting. Avocado trees are partially self-pollinating, and are often propagated through grafting to maintain predictable fruit quality and quantity. In 2017, Mexico produced 34% of the world supply of avocados.",
      "strThumb": "https://www.themealdb.com/images/ingredients/avocado.png",
      "strType": null
    },
    {
      "idIngredient": "9",
      "strIngredient": "Apple Cider Vinegar",
      "strDescription": "Apple cider vinegar, or cider vinegar, is a vinegar made from fermented apple juice, and used in salad dressings, marinades, vinaigrettes, food preservatives, and chutneys. It is made by crushing apples, then squeezing out the juice. Bacteria and yeast are added to the liquid to start the alcoholic fermentation process, which converts the sugars to alcohol. In a second fermentation step, the alcohol is converted into vinegar by acetic acid-forming bacteria (Acetobacter species). Acetic acid and malic acid combine to give vinegar its sour taste. Apple cider vinegar has no medicinal or nutritional value. There is no high-quality clinical evidence that regular consumption of apple cider vinegar helps to maintain or lose body weight, or is effective to manage blood glucose and lipid levels.",
      "strThumb": "https://www.themealdb.com/images/ingredients/apple_cider_vinegar.png",
      "strType": null
    },
    {
      "idIngredient": "10",
      "strIngredient": "Asparagus",
      "strDescription": "Asparagus, or garden asparagus, folk name sparrow grass, scientific name Asparagus officinalis, is a perennial flowering plant species in the genus Asparagus. Its young shoots are used as a spring vegetable.\r\n\r\nIt was once classified in the lily family, like the related Allium species, onions and garlic. However, genetic research places lilies, Allium, and asparagus in three separate families—the Liliaceae, Amaryllidaceae, and Asparagaceae, respectively—with the Amaryllidaceae and Asparagaceae being grouped together in the order Asparagales. Sources differ as to the native range of Asparagus officinalis, but generally include most of Europe and western temperate Asia. It is widely cultivated as a vegetable crop.",
      "strThumb": "https://www.themealdb.com/images/ingredients/asparagus.png",
      "strType": null
    },
    {
      "idIngredient": "11",
      "strIngredient": "Aubergine",
      "strDescription": "Eggplant (US, Australia), aubergine (UK), or brinjal (South Asia and South Africa) is a plant species in the nightshade family Solanaceae. Solanum melongena is grown worldwide for its edible fruit.\r\n\r\nMost commonly purple, the spongy, absorbent fruit is used in various cuisines. Although often considered a vegetable, it is a berry by botanical definition. As a member of the genus Solanum, it is related to tomato and potato. Like the tomato, its skin and seeds can be eaten, but, like the potato, it is usually eaten cooked. Eggplant is nutritionally low in macronutrient and micronutrient content, but the capability of the fruit to absorb oils and flavors into its flesh through cooking expands its use in the culinary arts.\r\n\r\nIt was originally domesticated from the wild nightshade species thorn or bitter apple, S. incanum, probably with two independent domestications: one in South Asia, and one in East Asia.",
      "strThumb": "https://www.themealdb.com/images/ingredients/aubergine.png",
      "strType": null
    },
    {
      "idIngredient": "13",
      "strIngredient": "Baby Plum Tomatoes",
      "strDescription": "The tomato is the edible, often red, berry of the plant Solanum lycopersicum, commonly known as a tomato plant. The species originated in western South America and Central America. The Nahuatl (Aztec language) word tomatl gave rise to the Spanish word tomate, from which the English word tomato derived.[3][4] Its domestication and use as a cultivated food may have originated with the indigenous peoples of Mexico. The Aztecs used tomatoes in their cooking at the time of the Spanish conquest of the Aztec Empire, and after the Spanish encountered the tomato for the first time after their contact with the Aztecs, they brought the plant to Europe. From there, the tomato was introduced to other parts of the European-colonized world during the 16th century.",
      "strThumb": "https://www.themealdb.com/images/ingredients/baby_plum_tomatoes.png",
      "strType": null
    },
    {
      "idIngredient": "14",
      "strIngredient": "Bacon",
      "strDescription": "Bacon is a type of salt-cured pork. Bacon is prepared from several different cuts of meat, typically from the pork belly or from back cuts, which have less fat than the belly. It is eaten on its own, as a side dish (particularly in breakfasts), or used as a minor ingredient to flavour dishes (e.g., the club sandwich). Bacon is also used for barding and larding roasts, especially game, including venison and pheasant. The word is derived from the Old High German bacho, meaning \"buttock\", \"ham\" or \"side of bacon\", and is cognate with the Old French bacon.",
      "strThumb": "https://www.themealdb.com/images/ingredients/bacon.png",
      "strType": null
    },
    {
      "idIngredient": "15",
      "strIngredient": "Baking Powder",
      "strDescription": "Baking powder is a dry chemical leavening agent, a mixture of a carbonate or bicarbonate and a weak acid. The base and acid are prevented from reacting prematurely by the inclusion of a buffer such as cornstarch. Baking powder is used to increase the volume and lighten the texture of baked goods. It works by releasing carbon dioxide gas into a batter or dough through an acid-base reaction, causing bubbles in the wet mixture to expand and thus leavening the mixture. The first single-acting baking powder was developed by Birmingham based food manufacturer Alfred Bird in England in 1843. The first double-acting baking powder was developed by Eben Norton Horsford in America in the 1860s.",
      "strThumb": "https://www.themealdb.com/images/ingredients/baking_powder.png",
      "strType": null
    },
    {
      "idIngredient": "16",
      "strIngredient": "Balsamic Vinegar",
      "strDescription": "Balsamic vinegar (Italian: aceto balsamico), occasionally shortened to balsamic, is a very dark, concentrated, and intensely flavoured vinegar originating in Italy, made wholly or partially from grape must. Grape must is freshly crushed grape juice with all the skins, seeds and stems.",
      "strThumb": "https://www.themealdb.com/images/ingredients/balsamic_vinegar.png",
      "strType": null
    },
    {
      "idIngredient": "17",
      "strIngredient": "Basil",
      "strDescription": "Basil, also called great basil, is a culinary herb of the family Lamiaceae (mints).\r\n\r\nBasil is native to tropical regions from central Africa to Southeast Asia. It is a tender plant, and is used in cuisines worldwide. Depending on the species and cultivar, the leaves may taste somewhat like anise, with a strong, pungent, often sweet smell.",
      "strThumb": "https://www.themealdb.com/images/ingredients/basil.png",
      "strType": null
    },
    {
      "idIngredient": "18",
      "strIngredient": "Basil Leaves",
      "strDescription": "Basil, also called great basil, is a culinary herb of the family Lamiaceae (mints).\r\n",
      "strThumb": "https://www.themealdb.com/images/ingredients/basil_leaves.png",
      "strType": null
    },
    {
      "idIngredient": "19",
      "strIngredient": "Basmati Rice",
      "strDescription": "Basmati is a variety of long, slender-grained aromatic rice which is traditionally from the Indian subcontinent. As of 2018-19, India exported to over 90% of the overseas basmati rice market, while Pakistan accounted for the remainder, according to the Indian state-run Agricultural and Processed Food Products Export Development Authority  and the Pakistan government-run Economic Survey of Pakistan. Many countries use domestically grown basmati rice crops; however, basmati is geographically exclusive to select districts of India, Pakistan, Nepal and Bangladesh.",
      "strThumb": "https://www.themealdb.com/images/ingredients/basmati_rice.png",
      "strType": null
    },
    {
      "idIngredient": "20",
      "strIngredient": "Bay Leaf",
      "strDescription": "The bay leaf is an aromatic leaf commonly used in cooking. It can be used whole, or as dried and ground.",
      "strThumb": "https://www.themealdb.com/images/ingredients/bay_leaf.png",
      "strType": null
    },
    {
      "idIngredient": "21",
      "strIngredient": "Bay Leaves",
      "strDescription": "The bay leaf is an aromatic leaf commonly used in cooking. It can be used whole, or as dried and ground.",
      "strThumb": "https://www.themealdb.com/images/ingredients/bay_leaves.png",
      "strType": null
    },
    {
      "idIngredient": "23",
      "strIngredient": "Beef Brisket",
      "strDescription": "Beef is the culinary name for meat from cattle, particularly skeletal muscle. Humans have been eating beef since prehistoric times. Beef is a source of high-quality protein and nutrients.\r\n\r\nMost beef skeletal muscle meat can be used as is by merely cutting into certain parts, such as roasts, short ribs or steak (filet mignon, sirloin steak, rump steak, rib steak, rib eye steak, hanger steak, etc.), while other cuts are processed (corned beef or beef jerky). Trimmings, on the other hand, are usually mixed with meat from older, leaner (therefore tougher) cattle, are ground, minced or used in sausages. The blood is used in some varieties called blood sausage. Other parts that are eaten include other muscles and offal, such as the oxtail, liver, tongue, tripe from the reticulum or rumen, glands (particularly the pancreas and thymus, referred to as sweetbread), the heart, the brain (although forbidden where there is a danger of bovine spongiform encephalopathy, BSE, commonly referred to as mad cow disease), the kidneys, and the tender testicles of the bull (known in the United States as calf fries, prairie oysters, or Rocky Mountain oysters). Some intestines are cooked and eaten as is, but are more often cleaned and used as natural sausage casings. The bones are used for making beef stock.",
      "strThumb": "https://www.themealdb.com/images/ingredients/beef_brisket.png",
      "strType": null
    },
    {
      "idIngredient": "24",
      "strIngredient": "Beef Fillet",
      "strDescription": "Beef is the culinary name for meat from cattle, particularly skeletal muscle. Humans have been eating beef since prehistoric times. Beef is a source of high-quality protein and nutrients.\r\n\r\nMost beef skeletal muscle meat can be used as is by merely cutting into certain parts, such as roasts, short ribs or steak (filet mignon, sirloin steak, rump steak, rib steak, rib eye steak, hanger steak, etc.), while other cuts are processed (corned beef or beef jerky). Trimmings, on the other hand, are usually mixed with meat from older, leaner (therefore tougher) cattle, are ground, minced or used in sausages. The blood is used in some varieties called blood sausage. Other parts that are eaten include other muscles and offal, such as the oxtail, liver, tongue, tripe from the reticulum or rumen, glands (particularly the pancreas and thymus, referred to as sweetbread), the heart, the brain (although forbidden where there is a danger of bovine spongiform encephalopathy, BSE, commonly referred to as mad cow disease), the kidneys, and the tender testicles of the bull (known in the United States as calf fries, prairie oysters, or Rocky Mountain oysters). Some intestines are cooked and eaten as is, but are more often cleaned and used as natural sausage casings. The bones are used for making beef stock.",
      "strThumb": "https://www.themealdb.com/images/ingredients/beef_fillet.png",
      "strType": null
    },
    {
      "idIngredient": "25",
      "strIngredient": "Beef Gravy",
      "strDescription": "Beef is the culinary name for meat from cattle, particularly skeletal muscle. Humans have been eating beef since prehistoric times. Beef is a source of high-quality protein and nutrients.\r\n\r\nMost beef skeletal muscle meat can be used as is by merely cutting into certain parts, such as roasts, short ribs or steak (filet mignon, sirloin steak, rump steak, rib steak, rib eye steak, hanger steak, etc.), while other cuts are processed (corned beef or beef jerky). Trimmings, on the other hand, are usually mixed with meat from older, leaner (therefore tougher) cattle, are ground, minced or used in sausages. The blood is used in some varieties called blood sausage. Other parts that are eaten include other muscles and offal, such as the oxtail, liver, tongue, tripe from the reticulum or rumen, glands (particularly the pancreas and thymus, referred to as sweetbread), the heart, the brain (although forbidden where there is a danger of bovine spongiform encephalopathy, BSE, commonly referred to as mad cow disease), the kidneys, and the tender testicles of the bull (known in the United States as calf fries, prairie oysters, or Rocky Mountain oysters). Some intestines are cooked and eaten as is, but are more often cleaned and used as natural sausage casings. The bones are used for making beef stock.",
      "strThumb": "https://www.themealdb.com/images/ingredients/beef_gravy.png",
      "strType": null
    },
    {
      "idIngredient": "26",
      "strIngredient": "Beef Stock",
      "strDescription": "Beef is the culinary name for meat from cattle, particularly skeletal muscle. Humans have been eating beef since prehistoric times. Beef is a source of high-quality protein and nutrients.\r\n\r\nMost beef skeletal muscle meat can be used as is by merely cutting into certain parts, such as roasts, short ribs or steak (filet mignon, sirloin steak, rump steak, rib steak, rib eye steak, hanger steak, etc.), while other cuts are processed (corned beef or beef jerky). Trimmings, on the other hand, are usually mixed with meat from older, leaner (therefore tougher) cattle, are ground, minced or used in sausages. The blood is used in some varieties called blood sausage. Other parts that are eaten include other muscles and offal, such as the oxtail, liver, tongue, tripe from the reticulum or rumen, glands (particularly the pancreas and thymus, referred to as sweetbread), the heart, the brain (although forbidden where there is a danger of bovine spongiform encephalopathy, BSE, commonly referred to as mad cow disease), the kidneys, and the tender testicles of the bull (known in the United States as calf fries, prairie oysters, or Rocky Mountain oysters). Some intestines are cooked and eaten as is, but are more often cleaned and used as natural sausage casings. The bones are used for making beef stock.",
      "strThumb": "https://www.themealdb.com/images/ingredients/beef_stock.png",
      "strType": null
    },
    {
      "idIngredient": "27",
      "strIngredient": "Bicarbonate Of Soda",
      "strDescription": "Sodium bicarbonate, commonly known as baking soda, is a chemical compound with the formula NaHCO3. It is a salt composed of a sodium cation (Na+) and a bicarbonate anion (HCO3−). Sodium bicarbonate is a white solid that is crystalline, but often appears as a fine powder. It has a slightly salty, alkaline taste resembling that of washing soda (sodium carbonate). The natural mineral form is nahcolite. It is a component of the mineral natron and is found dissolved in many mineral springs.",
      "strThumb": "https://www.themealdb.com/images/ingredients/bicarbonate_of_soda.png",
      "strType": null
    },
    {
      "idIngredient": "28",
      "strIngredient": "Biryani Masala",
      "strDescription": "Biryani, also known as biriyani, biriani, birani or briyani, is a mixed rice dish with its origins among the Muslims of the Indian subcontinent. It can be compared to mixing a curry, later combining it with semi-cooked rice separately. This dish is especially popular throughout the Indian subcontinent, as well as among the diaspora from the region. It is also prepared in other regions such as Iraqi Kurdistan. It is made with Indian spices, rice, meat (chicken, goat, beef, lamb, prawn, or fish), vegetables or eggs.",
      "strThumb": "https://www.themealdb.com/images/ingredients/biryani_masala.png",
      "strType": null
    },
    {
      "idIngredient": "29",
      "strIngredient": "Black Pepper",
      "strDescription": "Black pepper (Piper nigrum) is a flowering vine in the family Piperaceae, cultivated for its fruit, known as a peppercorn, which is usually dried and used as a spice and seasoning. When fresh and fully mature, it is about 5 mm (0.20 in) in diameter and dark red, and contains a single seed, like all drupes. Peppercorns and the ground pepper derived from them may be described simply as pepper, or more precisely as black pepper (cooked and dried unripe fruit), green pepper (dried unripe fruit), or white pepper (ripe fruit seeds).\r\n\r\nBlack pepper is native to present-day Kerala in Southwestern India, and is extensively cultivated there and elsewhere in tropical regions. Vietnam is the world's largest producer and exporter of pepper, producing 34% of the world's crop, as of 2013.",
      "strThumb": "https://www.themealdb.com/images/ingredients/black_pepper.png",
      "strType": null
    },
    {
      "idIngredient": "30",
      "strIngredient": "Black Treacle",
      "strDescription": "Molasses (American English) or black treacle (British English) is a viscous product resulting from refining sugarcane or sugar beets into sugar. Molasses varies by amount of sugar, method of extraction, and age of plant. Sugarcane molasses is primarily used for sweetening and flavoring foods in the United States, Canada, and elsewhere. Molasses is a defining component of fine commercial brown sugar.\r\n\r\nSweet sorghum syrup may be colloquially called \"sorghum molasses\" in the southern United States.[2][3] Similar products include honey, maple syrup, corn syrup, and invert syrup. Most of these alternative syrups have milder flavors.",
      "strThumb": "https://www.themealdb.com/images/ingredients/black_treacle.png",
      "strType": null
    },
    {
      "idIngredient": "31",
      "strIngredient": "Borlotti Beans",
      "strDescription": null,
      "strThumb": "https://www.themealdb.com/images/ingredients/borlotti_beans.png",
      "strType": null
    },
    {
      "idIngredient": "32",
      "strIngredient": "Bowtie Pasta",
      "strDescription": null,
      "strThumb": "https://www.themealdb.com/images/ingredients/bowtie_pasta.png",
      "strType": null
    },
```

## Filter by main ingredient
www.themealdb.com/api/json/v1/1/filter.php?i=chicken_breast
```json
{
  "meals": [
    {
      "strMeal": "Chick-Fil-A Sandwich",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/sbx7n71587673021.jpg",
      "idMeal": "53016"
    },
    {
      "strMeal": "Chicken Couscous",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/qxytrx1511304021.jpg",
      "idMeal": "52850"
    },
    {
      "strMeal": "Chicken Fajita Mac and Cheese",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/qrqywr1503066605.jpg",
      "idMeal": "52818"
    },
    {
      "strMeal": "Chicken Ham and Leek Pie",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/xrrtss1511555269.jpg",
      "idMeal": "52875"
    },
    {
      "strMeal": "Chicken Quinoa Greek Salad",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/k29viq1585565980.jpg",
      "idMeal": "53011"
    },
    {
      "strMeal": "General Tsos Chicken",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1529444113.jpg",
      "idMeal": "52951"
    },
    {
      "strMeal": "Honey Balsamic Chicken with Crispy Broccoli & Potatoes",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/kvbotn1581012881.jpg",
      "idMeal": "52993"
    },
    {
      "strMeal": "Katsu Chicken curry",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/vwrpps1503068729.jpg",
      "idMeal": "52820"
    },
    {
      "strMeal": "Rappie Pie",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/ruwpww1511817242.jpg",
      "idMeal": "52933"
    }
  ]
}
```

## Filter by Category
www.themealdb.com/api/json/v1/1/filter.php?c=Seafood
```json
{
  "meals": [
    {
      "strMeal": "Baked salmon with fennel & tomatoes",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1548772327.jpg",
      "idMeal": "52959"
    },
    {
      "strMeal": "Barramundi with Moroccan spices",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/4o4wh11761848573.jpg",
      "idMeal": "53103"
    },
    {
      "strMeal": "Cajun spiced fish tacos",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/uvuyxu1503067369.jpg",
      "idMeal": "52819"
    },
    {
      "strMeal": "Escovitch Fish",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1520084413.jpg",
      "idMeal": "52944"
    },
    {
      "strMeal": "Fish fofos",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/a15wsa1614349126.jpg",
      "idMeal": "53043"
    },
    {
      "strMeal": "Fish pie",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/ysxwuq1487323065.jpg",
      "idMeal": "52802"
    },
    {
      "strMeal": "Fish Soup (Ukha)",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/7n8su21699013057.jpg",
      "idMeal": "53079"
    },
    {
      "strMeal": "Fish Stew with Rouille",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/vptqpw1511798500.jpg",
      "idMeal": "52918"
    },
    {
      "strMeal": "Garides Saganaki",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wuvryu1468232995.jpg",
      "idMeal": "52764"
    },
    {
      "strMeal": "Grilled Portuguese sardines",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/lpd4wy1614347943.jpg",
      "idMeal": "53041"
    },
    {
      "strMeal": "Honey Teriyaki Salmon",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/xxyupu1468262513.jpg",
      "idMeal": "52773"
    },
    {
      "strMeal": "Kedgeree",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/utxqpt1511639216.jpg",
      "idMeal": "52887"
    },
    {
      "strMeal": "Kung Po Prawns",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1525873040.jpg",
      "idMeal": "52946"
    },
    {
      "strMeal": "Laksa King Prawn Noodles",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/rvypwy1503069308.jpg",
      "idMeal": "52821"
    },
    {
      "strMeal": "Mediterranean Pasta Salad",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wvqpwt1468339226.jpg",
      "idMeal": "52777"
    },
    {
      "strMeal": "Mee goreng mamak",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/xquakq1619787532.jpg",
      "idMeal": "53048"
    },
    {
      "strMeal": "Nasi lemak",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wai9bw1619788844.jpg",
      "idMeal": "53051"
    },
    {
      "strMeal": "Portuguese fish stew (Caldeirada de peixe)",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/do7zps1614349775.jpg",
      "idMeal": "53045"
    },
    {
      "strMeal": "Quick salt & pepper squid",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/cp74zo1762341241.jpg",
      "idMeal": "53108"
    },
    {
      "strMeal": "Recheado Masala Fish",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/uwxusv1487344500.jpg",
      "idMeal": "52809"
    },
    {
      "strMeal": "Salmon Avocado Salad",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1549542994.jpg",
      "idMeal": "52960"
    },
    {
      "strMeal": "Salmon Prawn Risotto",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/xxrxux1503070723.jpg",
      "idMeal": "52823"
    },
    {
      "strMeal": "Saltfish and Ackee",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/vytypy1511883765.jpg",
      "idMeal": "52936"
    },
    {
      "strMeal": "Seafood fideuà",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wqqvyq1511179730.jpg",
      "idMeal": "52836"
    },
    {
      "strMeal": "Shrimp Chow Fun",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1529445434.jpg",
      "idMeal": "52953"
    },
    {
      "strMeal": "Sledz w Oleju (Polish Herrings)",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/7ttta31593350374.jpg",
      "idMeal": "53023"
    },
    {
      "strMeal": "Spring onion and prawn empanadas",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1c5oso1614347493.jpg",
      "idMeal": "53040"
    },
    {
      "strMeal": "Squid, chickpea & chorizo salad",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wsu0rc1761848482.jpg",
      "idMeal": "53102"
    },
    {
      "strMeal": "Sushi",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/g046bb1663960946.jpg",
      "idMeal": "53065"
    },
    {
      "strMeal": "Three Fish Pie",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/spswqs1511558697.jpg",
      "idMeal": "52882"
    },
    {
      "strMeal": "Tuna and Egg Briks",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/2dsltq1560461468.jpg",
      "idMeal": "52975"
    },
    {
      "strMeal": "Tuna Nicoise",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/yypwwq1511304979.jpg",
      "idMeal": "52852"
    }
  ]
}
```

## Filter by Area
www.themealdb.com/api/json/v1/1/filter.php?a=Canadian
```json
{
  "meals": [
    {
      "strMeal": "BeaverTails",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/ryppsv1511815505.jpg",
      "idMeal": "52928"
    },
    {
      "strMeal": "Breakfast Potatoes",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1550441882.jpg",
      "idMeal": "52965"
    },
    {
      "strMeal": "Canadian Butter Tarts",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/wpputp1511812960.jpg",
      "idMeal": "52923"
    },
    {
      "strMeal": "Montreal Smoked Meat",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/uttupv1511815050.jpg",
      "idMeal": "52927"
    },
    {
      "strMeal": "Nanaimo Bars",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/vwuprt1511813703.jpg",
      "idMeal": "52924"
    },
    {
      "strMeal": "Pate Chinois",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/yyrrxr1511816289.jpg",
      "idMeal": "52930"
    },
    {
      "strMeal": "Pouding chomeur",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/yqqqwu1511816912.jpg",
      "idMeal": "52932"
    },
    {
      "strMeal": "Poutine",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/uuyrrx1487327597.jpg",
      "idMeal": "52804"
    },
    {
      "strMeal": "Rappie Pie",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/ruwpww1511817242.jpg",
      "idMeal": "52933"
    },
    {
      "strMeal": "Split Pea Soup",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/xxtsvx1511814083.jpg",
      "idMeal": "52925"
    },
    {
      "strMeal": "Sugar Pie",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/yrstur1511816601.jpg",
      "idMeal": "52931"
    },
    {
      "strMeal": "Timbits",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/txsupu1511815755.jpg",
      "idMeal": "52929"
    },
    {
      "strMeal": "Tourtiere",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/ytpstt1511814614.jpg",
      "idMeal": "52926"
    }
  ]
}
```

## Meal Thumbnail Images
Add /preview to the end of the meal image URL
/images/media/meals/llcbn01574260722.jpg/small
/images/media/meals/llcbn01574260722.jpg/medium
/images/media/meals/llcbn01574260722.jpg/large

## Ingredient Thumbnail Images
*URL's match the ingredient name with an underscore for any spaces.

www.themealdb.com/images/ingredients/lime.png
www.themealdb.com/images/ingredients/lime-small.png
www.themealdb.com/images/ingredients/lime-medium.png
www.themealdb.com/images/ingredients/lime-large.png

