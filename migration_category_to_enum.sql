-- Migration script to convert category from Korean text to enum values
-- This script updates existing group_buys records to use enum constant names instead of Korean display names

-- Mapping:
-- '레시피' → 'RECIPE'
-- '육류' → 'MEAT'
-- '수산물' → 'SEAFOOD'
-- '채소' → 'VEGETABLE'
-- '곡물/쌀' → 'GRAIN'
-- '과일' → 'FRUIT'
-- '유제품' → 'DAIRY'
-- '양념/소스' → 'SEASONING'
-- '간식/후식' → 'SNACK'
-- '기타' → 'ETC'

-- First, check current category values
SELECT category, COUNT(*) as count 
FROM group_buys 
WHERE deleted_at IS NULL
GROUP BY category;

-- Update categories to enum values
UPDATE group_buys SET category = 'MEAT' WHERE category = '육류';
UPDATE group_buys SET category = 'SEAFOOD' WHERE category = '수산물';
UPDATE group_buys SET category = 'VEGETABLE' WHERE category = '채소';
UPDATE group_buys SET category = 'GRAIN' WHERE category = '곡물/쌀';
UPDATE group_buys SET category = 'FRUIT' WHERE category = '과일';
UPDATE group_buys SET category = 'DAIRY' WHERE category = '유제품';
UPDATE group_buys SET category = 'SEASONING' WHERE category = '양념/소스';
UPDATE group_buys SET category = 'SNACK' WHERE category = '간식/후식';
UPDATE group_buys SET category = 'ETC' WHERE category = '기타';

-- For recipe-based group buys (have recipeApiId), set to RECIPE if not already set
UPDATE group_buys 
SET category = 'RECIPE' 
WHERE recipe_api_id IS NOT NULL 
  AND category NOT IN ('RECIPE', 'MEAT', 'SEAFOOD', 'VEGETABLE', 'GRAIN', 'FRUIT', 'DAIRY', 'SEASONING', 'SNACK', 'ETC');

-- Verify results
SELECT category, COUNT(*) as count 
FROM group_buys 
WHERE deleted_at IS NULL
GROUP BY category;

-- Check for any unmapped categories (should return no rows)
SELECT DISTINCT category 
FROM group_buys 
WHERE category NOT IN ('RECIPE', 'MEAT', 'SEAFOOD', 'VEGETABLE', 'GRAIN', 'FRUIT', 'DAIRY', 'SEASONING', 'SNACK', 'ETC')
  AND deleted_at IS NULL;
