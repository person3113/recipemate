-- Migration: Remove DESCRIPTION column from RECIPES table
-- Date: 2025-11-08
-- Reason: The description field is not used in templates and wastes storage space
--         - TheMealDB recipes always use manualSteps (from recipeSteps) instead of instructions
--         - FoodSafety recipes always have null description
-- Impact: Removes unused TEXT column (~200 chars per recipe)

-- Execute this in H2 Console (http://localhost:8080/h2-console)

ALTER TABLE RECIPES DROP COLUMN DESCRIPTION;
