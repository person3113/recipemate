- 개발환경. themaldb 레시피 동기화 스크립트

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/recipes/admin/sync-mealdb?count=100" -Method POST
```

- h2-console에서 레시피 재료 조회

```sql
SELECT title, name, measure
FROM RECIPE_INGREDIENTS
JOIN recipes
ON RECIPE_INGREDIENTS.recipe_id = recipes.id
WHERE recipes.SOURCE_API = 'FOOD_SAFETY';
```

- 관리자 테스트용 계정
admin1@recipemate.com
admin123
관리자1
010-1111-1111