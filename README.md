- 개발환경. themealdb 레시피 동기화 스크립트

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/recipes/admin/sync-mealdb?count=100" -Method POST
```