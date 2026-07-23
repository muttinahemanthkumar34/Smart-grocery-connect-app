-- ============================================
-- GROCERY CONNECT - AI ASSISTANT DATABASE
-- Rule-Based Knowledge System for Recipes
-- ============================================

-- Create recipes table for AI knowledge base
CREATE TABLE IF NOT EXISTS recipes (
    recipe_id INT AUTO_INCREMENT PRIMARY KEY,
    dish_name VARCHAR(100) NOT NULL,
    ingredients TEXT NOT NULL,
    steps TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    keywords VARCHAR(255) DEFAULT NULL COMMENT 'Additional keywords for better matching',
    prep_time VARCHAR(20) DEFAULT '30 mins',
    difficulty VARCHAR(20) DEFAULT 'Easy',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- SAMPLE RECIPE DATA (13 dishes)
-- ============================================

-- Indian Dishes
INSERT INTO recipes (dish_name, ingredients, steps, category, keywords, prep_time, difficulty) VALUES
('Chicken Biryani', 
 'Basmati rice, Chicken, Onions, Tomatoes, Yogurt, Ginger-garlic paste, Biryani masala, Saffron, Mint leaves, Coriander leaves, Green chilies, Oil, Salt',
 '1. Marinate chicken with yogurt and spices for 1 hour|2. Soak basmati rice for 30 minutes|3. Fry onions until golden brown|4. Add marinated chicken and cook|5. Layer rice and chicken alternately|6. Add saffron milk and dum cook for 25 minutes|7. Garnish with fried onions and serve',
 'Indian',
 'biryani, rice, chicken, hyderabadi, dum',
 '1 hour',
 'Medium'),

('Paneer Butter Masala',
 'Paneer, Tomatoes, Onions, Cashews, Butter, Cream, Ginger-garlic paste, Kashmiri red chili, Garam masala, Kasuri methi, Salt, Sugar',
 '1. Blend tomatoes, onions, and cashews into smooth paste|2. Heat butter and saute ginger-garlic paste|3. Add the blended paste and cook for 10 minutes|4. Add spices, cream, and kasuri methi|5. Add paneer cubes and simmer for 5 minutes|6. Garnish with cream and serve with naan',
 'Indian',
 'paneer, butter, masala, curry, vegetarian, gravy',
 '40 mins',
 'Easy'),

('Dal Tadka',
 'Toor dal, Onions, Tomatoes, Garlic, Cumin seeds, Mustard seeds, Red chilies, Turmeric, Coriander powder, Ghee, Coriander leaves, Salt',
 '1. Wash and pressure cook dal with turmeric|2. Mash the cooked dal|3. Heat ghee and add cumin, mustard seeds|4. Add garlic, onions, and fry until golden|5. Add tomatoes and spices, cook until soft|6. Pour tadka over dal and mix|7. Garnish with coriander and serve with rice',
 'Indian',
 'dal, lentils, tadka, vegetarian, healthy, protein',
 '30 mins',
 'Easy'),

-- Italian Dishes
('Spaghetti Pasta',
 'Spaghetti, Tomato sauce, Garlic, Olive oil, Onions, Basil, Oregano, Parmesan cheese, Salt, Black pepper, Chili flakes',
 '1. Boil spaghetti in salted water until al dente|2. Heat olive oil and saute garlic|3. Add onions and cook until translucent|4. Add tomato sauce, oregano, and basil|5. Simmer for 10 minutes|6. Toss cooked pasta with sauce|7. Top with parmesan and serve',
 'Italian',
 'pasta, spaghetti, italian, tomato, noodles',
 '25 mins',
 'Easy'),

('Margherita Pizza',
 'Pizza dough, Tomato sauce, Mozzarella cheese, Fresh basil, Olive oil, Garlic, Salt, Oregano',
 '1. Preheat oven to 250°C|2. Roll out pizza dough into circle|3. Spread tomato sauce evenly|4. Add mozzarella cheese|5. Drizzle olive oil and add oregano|6. Bake for 12-15 minutes until crust is golden|7. Top with fresh basil and serve',
 'Italian',
 'pizza, cheese, italian, baked, margherita',
 '30 mins',
 'Easy'),

-- Chinese Dishes
('Vegetable Fried Rice',
 'Cooked rice, Mixed vegetables, Eggs, Soy sauce, Garlic, Ginger, Green onions, Sesame oil, Salt, Pepper, Oil',
 '1. Heat oil in wok on high heat|2. Scramble eggs and set aside|3. Stir-fry garlic, ginger, and vegetables|4. Add cold cooked rice|5. Add soy sauce and toss well|6. Add scrambled eggs back|7. Garnish with green onions and serve',
 'Chinese',
 'rice, fried rice, chinese, vegetables, quick',
 '20 mins',
 'Easy'),

('Manchurian',
 'Cabbage, Carrots, Capsicum, Corn flour, All-purpose flour, Soy sauce, Vinegar, Chili sauce, Garlic, Ginger, Green onions, Oil, Salt',
 '1. Grate vegetables and mix with flours|2. Shape into balls and deep fry until golden|3. For sauce: heat oil, add garlic-ginger|4. Add sauces and stir|5. Add fried balls to sauce|6. Toss well and garnish with green onions|7. Serve hot as starter or with rice',
 'Chinese',
 'manchurian, indo-chinese, starter, vegetables, crispy',
 '35 mins',
 'Medium'),

-- Continental
('Grilled Cheese Sandwich',
 'Bread slices, Cheese slices, Butter, Optional: tomatoes, onions, jalapenos',
 '1. Butter one side of each bread slice|2. Place cheese between bread slices|3. Add optional vegetables|4. Heat pan on medium|5. Grill sandwich until golden on both sides|6. Cut diagonally and serve hot',
 'Continental',
 'sandwich, cheese, grilled, quick, breakfast, snack',
 '10 mins',
 'Easy'),

('Caesar Salad',
 'Romaine lettuce, Croutons, Parmesan cheese, Caesar dressing, Olive oil, Lemon juice, Garlic, Black pepper',
 '1. Wash and chop romaine lettuce|2. Prepare caesar dressing with garlic, lemon, olive oil|3. Toss lettuce with dressing|4. Add croutons|5. Top with shaved parmesan|6. Season with black pepper|7. Serve immediately',
 'Continental',
 'salad, caesar, healthy, lettuce, light, diet',
 '15 mins',
 'Easy'),

-- Desserts
('Chocolate Cake',
 'All-purpose flour, Cocoa powder, Sugar, Eggs, Butter, Milk, Baking powder, Vanilla extract, Chocolate frosting',
 '1. Preheat oven to 180°C|2. Mix flour, cocoa, baking powder|3. Beat butter and sugar until fluffy|4. Add eggs and vanilla|5. Alternately add dry ingredients and milk|6. Pour into greased pan and bake 30 mins|7. Cool and frost with chocolate frosting',
 'Dessert',
 'cake, chocolate, sweet, baking, birthday, dessert',
 '1 hour',
 'Medium'),

('Pancakes',
 'All-purpose flour, Milk, Eggs, Sugar, Butter, Baking powder, Salt, Maple syrup, Fresh fruits',
 '1. Mix flour, baking powder, sugar, salt|2. Whisk milk, eggs, melted butter|3. Combine wet and dry ingredients|4. Heat pan and grease lightly|5. Pour batter and cook until bubbles form|6. Flip and cook other side|7. Stack and serve with maple syrup and fruits',
 'Dessert',
 'pancake, breakfast, sweet, fluffy, american',
 '20 mins',
 'Easy'),

-- South Indian
('Masala Dosa',
 'Dosa batter, Potatoes, Onions, Mustard seeds, Curry leaves, Turmeric, Green chilies, Oil, Salt, Coconut chutney, Sambar',
 '1. Boil and mash potatoes|2. Heat oil, add mustard seeds and curry leaves|3. Add onions, chilies, turmeric|4. Add mashed potatoes and mix well|5. Spread dosa batter on hot tawa|6. Drizzle oil and cook until crispy|7. Place potato filling and fold|8. Serve with chutney and sambar',
 'South Indian',
 'dosa, masala, south indian, breakfast, crispy, vegetarian',
 '30 mins',
 'Medium'),

('Idli Sambar',
 'Idli batter, Toor dal, Mixed vegetables, Tamarind, Sambar powder, Mustard seeds, Curry leaves, Asafoetida, Oil, Salt, Coconut chutney',
 '1. Steam idli batter in idli molds for 12 mins|2. Cook dal until soft|3. Cook vegetables separately|4. Add tamarind water and sambar powder to dal|5. Add vegetables and simmer|6. Prepare tempering with mustard, curry leaves|7. Serve idlis hot with sambar and chutney',
 'South Indian',
 'idli, sambar, south indian, breakfast, healthy, steamed',
 '35 mins',
 'Easy');

-- Create index for faster keyword searching
CREATE INDEX idx_dish_name ON recipes(dish_name);
CREATE INDEX idx_category ON recipes(category);
CREATE INDEX idx_keywords ON recipes(keywords);
