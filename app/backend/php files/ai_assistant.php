<?php
/**
 * ============================================
 * GROCERY CONNECT - AI ASSISTANT API
 * Advanced Rule-Based Knowledge System
 * ============================================
 * 
 * This AI assistant uses an advanced RULE-BASED approach:
 * 1. Natural Language Understanding (NLU) with pattern matching
 * 2. Intent classification from user input
 * 3. Entity extraction for recipes, categories, preferences
 * 4. Context-aware response generation
 * 
 * NO external AI APIs, machine learning, or cloud services used.
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db.php';
$pdo = $conn;

/**
 * ============================================
 * NATURAL LANGUAGE UNDERSTANDING (NLU) ENGINE
 * ============================================
 */

/**
 * Detect user intent from input text
 */
function detectIntent($input) {
    // Normalize input
    $input = strtolower(trim($input));
    $input = preg_replace('/[^a-z0-9\s]/', '', $input);
    
    // Intent patterns with keywords and phrases
    $intents = [
        'greeting' => [
            'exact' => ['hi', 'hello', 'hey', 'hii', 'hiii', 'yo', 'hola'],
            'starts_with' => ['good morning', 'good afternoon', 'good evening', 'good night', 'hi there', 'hello there', 'hey there'],
            'contains' => ['how are you', 'whats up', 'wassup']
        ],
        'farewell' => [
            'exact' => ['bye', 'goodbye', 'cya', 'later'],
            'contains' => ['see you', 'talk later', 'gotta go', 'have to go']
        ],
        'thanks' => [
            'exact' => ['thanks', 'thank you', 'thx', 'ty'],
            'contains' => ['thanks a lot', 'thank you so much', 'appreciate it']
        ],
        'help' => [
            'exact' => ['help', 'menu', 'options'],
            'contains' => ['what can you do', 'how to use', 'help me', 'guide me', 'what do you do', 'your features', 'show menu']
        ],
        'random' => [
            'contains' => ['random', 'surprise me', 'anything', 'whatever', 'any recipe', 'any dish', 'something random']
        ],
        'veg' => [
            'exact' => ['veg', 'vegetarian', 'veggie'],
            'contains' => ['veg recipe', 'veg dish', 'vegetarian recipe', 'vegetarian dish', 'veggie recipe', 'without meat', 'no meat', 'pure veg']
        ],
        'nonveg' => [
            'exact' => ['nonveg', 'non veg', 'nonvegetarian'],
            'contains' => ['non veg recipe', 'non veg dish', 'nonveg recipe', 'meat recipe', 'with meat', 'chicken recipe', 'mutton recipe']
        ],
        'easy' => [
            'contains' => ['easy recipe', 'simple recipe', 'quick recipe', 'beginner recipe', 'easy to make', 'simple to make', 'fast recipe']
        ],
        'dessert' => [
            'exact' => ['dessert', 'desserts', 'sweet', 'sweets'],
            'contains' => ['sweet dish', 'dessert recipe', 'something sweet', 'want sweet', 'need dessert']
        ],
        'indian' => [
            'exact' => ['indian'],
            'contains' => ['indian recipe', 'indian dish', 'indian food', 'desi recipe', 'desi food']
        ],
        'southindian' => [
            'exact' => ['dosa', 'idli', 'sambar'],
            'contains' => ['south indian', 'tamil recipe', 'kerala recipe', 'andhra recipe']
        ],
        'italian' => [
            'exact' => ['italian', 'pasta', 'pizza'],
            'contains' => ['italian recipe', 'italian dish', 'italian food']
        ],
        'chinese' => [
            'exact' => ['chinese', 'noodles', 'manchurian'],
            'contains' => ['chinese recipe', 'chinese dish', 'chinese food', 'indo chinese']
        ],
        'continental' => [
            'exact' => ['continental'],
            'contains' => ['continental recipe', 'continental dish', 'western food']
        ],
        'breakfast' => [
            'exact' => ['breakfast'],
            'contains' => ['breakfast recipe', 'morning recipe', 'breakfast dish', 'for breakfast']
        ],
        'lunch' => [
            'exact' => ['lunch'],
            'contains' => ['lunch recipe', 'lunch dish', 'for lunch']
        ],
        'dinner' => [
            'exact' => ['dinner'],
            'contains' => ['dinner recipe', 'dinner dish', 'for dinner', 'evening meal']
        ],
        'snack' => [
            'exact' => ['snack', 'snacks'],
            'contains' => ['snack recipe', 'evening snack', 'teatime snack', 'quick snack']
        ],
        'suggest' => [
            'contains' => ['suggest', 'recommend', 'give me', 'show me', 'tell me', 'list', 'what should i', 'what can i', 'ideas for']
        ],
        'how_to' => [
            'starts_with' => ['how to make', 'how to cook', 'how to prepare', 'how do i make', 'how do you make'],
            'contains' => ['recipe for', 'make a', 'cook a', 'prepare a']
        ]
    ];
    
    // Check each intent
    foreach ($intents as $intentName => $patterns) {
        // Check exact matches
        if (isset($patterns['exact'])) {
            foreach ($patterns['exact'] as $pattern) {
                if ($input === $pattern) {
                    return ['type' => $intentName, 'confidence' => 1.0];
                }
            }
        }
        
        // Check starts_with patterns
        if (isset($patterns['starts_with'])) {
            foreach ($patterns['starts_with'] as $pattern) {
                if (strpos($input, $pattern) === 0) {
                    return ['type' => $intentName, 'confidence' => 0.9];
                }
            }
        }
        
        // Check contains patterns
        if (isset($patterns['contains'])) {
            foreach ($patterns['contains'] as $pattern) {
                if (strpos($input, $pattern) !== false) {
                    return ['type' => $intentName, 'confidence' => 0.8];
                }
            }
        }
    }
    
    // Default to recipe search
    return ['type' => 'recipe_search', 'confidence' => 0.5];
}

/**
 * Extract dish name from input
 */
function extractDishName($input) {
    // Remove common prefixes
    $prefixes = [
        'how to make', 'how to cook', 'how to prepare', 'how do i make',
        'recipe for', 'make a', 'cook a', 'prepare a',
        'give me', 'show me', 'tell me', 'i want', 'i need',
        'can you give', 'can you show', 'please give', 'please show'
    ];
    
    $cleaned = strtolower(trim($input));
    foreach ($prefixes as $prefix) {
        if (strpos($cleaned, $prefix) === 0) {
            $cleaned = trim(substr($cleaned, strlen($prefix)));
        }
    }
    
    // Remove common suffixes
    $suffixes = ['recipe', 'dish', 'please', 'thanks'];
    foreach ($suffixes as $suffix) {
        $cleaned = preg_replace('/\s*' . $suffix . '\s*$/i', '', $cleaned);
    }
    
    return trim($cleaned);
}

/**
 * Input normalization
 */
function normalizeInput($input) {
    $input = strtolower(trim($input));
    $input = preg_replace('/[^a-z0-9\s]/', '', $input);
    $input = preg_replace('/\s+/', ' ', $input);
    return $input;
}

/**
 * Keyword extraction
 */
function extractKeywords($input) {
    $stopWords = ['a', 'an', 'the', 'is', 'are', 'was', 'were', 'how', 'to', 'make', 
                  'cook', 'prepare', 'recipe', 'for', 'of', 'and', 'with', 'what',
                  'give', 'me', 'tell', 'show', 'i', 'want', 'need', 'please', 'can', 'you',
                  'suggest', 'recommend', 'some', 'any', 'good', 'best', 'tasty', 'do'];
    
    $words = explode(' ', $input);
    $keywords = array_filter($words, function($word) use ($stopWords) {
        return strlen($word) > 1 && !in_array($word, $stopWords);
    });
    
    return array_values($keywords);
}

/**
 * ============================================
 * DATABASE QUERY FUNCTIONS
 * ============================================
 */

function searchRecipes($pdo, $keywords, $originalQuery) {
    // Exact match
    $stmt = $pdo->prepare("SELECT * FROM recipes WHERE LOWER(dish_name) = ?");
    $stmt->execute([$originalQuery]);
    $exactMatch = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($exactMatch) {
        return ['recipes' => [$exactMatch], 'matchType' => 'exact'];
    }
    
    // Partial name match
    $stmt = $pdo->prepare("SELECT * FROM recipes WHERE LOWER(dish_name) LIKE ?");
    $stmt->execute(["%$originalQuery%"]);
    $nameMatches = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (!empty($nameMatches)) {
        return ['recipes' => $nameMatches, 'matchType' => 'partial'];
    }
    
    // Keyword match
    if (!empty($keywords)) {
        $conditions = [];
        $params = [];
        
        foreach ($keywords as $keyword) {
            $conditions[] = "(LOWER(dish_name) LIKE ? OR LOWER(keywords) LIKE ? OR LOWER(category) LIKE ? OR LOWER(ingredients) LIKE ?)";
            $params = array_merge($params, ["%$keyword%", "%$keyword%", "%$keyword%", "%$keyword%"]);
        }
        
        $sql = "SELECT * FROM recipes WHERE " . implode(' OR ', $conditions);
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $keywordMatches = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        if (!empty($keywordMatches)) {
            return ['recipes' => $keywordMatches, 'matchType' => 'keyword'];
        }
    }
    
    return ['recipes' => [], 'matchType' => 'none'];
}

function getRecipesByCategory($pdo, $category) {
    $stmt = $pdo->prepare("SELECT * FROM recipes WHERE category = ?");
    $stmt->execute([$category]);
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

function getVegRecipes($pdo) {
    $stmt = $pdo->query("SELECT * FROM recipes WHERE LOWER(ingredients) NOT LIKE '%chicken%' AND LOWER(ingredients) NOT LIKE '%mutton%' AND LOWER(ingredients) NOT LIKE '%fish%' AND LOWER(ingredients) NOT LIKE '%egg%' AND LOWER(dish_name) NOT LIKE '%chicken%' AND LOWER(dish_name) NOT LIKE '%mutton%' AND LOWER(dish_name) NOT LIKE '%fish%'");
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

function getNonVegRecipes($pdo) {
    $stmt = $pdo->query("SELECT * FROM recipes WHERE LOWER(ingredients) LIKE '%chicken%' OR LOWER(ingredients) LIKE '%mutton%' OR LOWER(ingredients) LIKE '%fish%' OR LOWER(dish_name) LIKE '%chicken%' OR LOWER(dish_name) LIKE '%mutton%'");
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

function getEasyRecipes($pdo) {
    $stmt = $pdo->query("SELECT * FROM recipes WHERE difficulty = 'Easy' OR difficulty = 'Beginner'");
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

function getRandomRecipes($pdo, $limit = 5) {
    $stmt = $pdo->query("SELECT * FROM recipes ORDER BY RAND() LIMIT $limit");
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}

function getPopularSuggestions($pdo, $limit = 5) {
    $stmt = $pdo->query("SELECT dish_name FROM recipes ORDER BY recipe_id LIMIT $limit");
    return $stmt->fetchAll(PDO::FETCH_COLUMN);
}

/**
 * ============================================
 * RESPONSE GENERATION
 * ============================================
 */

function formatRecipe($recipe) {
    return [
        'recipe_id' => $recipe['recipe_id'],
        'dish_name' => $recipe['dish_name'],
        'category' => $recipe['category'],
        'prep_time' => $recipe['prep_time'] ?? '30 mins',
        'difficulty' => $recipe['difficulty'] ?? 'Easy',
        'ingredients' => array_map('trim', explode(',', $recipe['ingredients'])),
        'steps' => array_map('trim', explode('|', $recipe['steps']))
    ];
}

function createRecipeList($recipes) {
    return array_map(function($r) {
        return [
            'recipe_id' => $r['recipe_id'],
            'dish_name' => $r['dish_name'],
            'category' => $r['category'],
            'prep_time' => $r['prep_time'] ?? '30 mins'
        ];
    }, $recipes);
}

function generateResponse($type, $message, $data = null) {
    $response = [
        'status' => true,
        'type' => $type,
        'message' => $message,
        'timestamp' => date('Y-m-d H:i:s')
    ];
    
    if ($data !== null) {
        foreach ($data as $key => $value) {
            $response[$key] = $value;
        }
    }
    
    return $response;
}

// ============================================
// MAIN EXECUTION
// ============================================

$query = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $query = $_POST['query'] ?? '';
    if (empty($query)) {
        $jsonBody = json_decode(file_get_contents('php://input'), true);
        $query = $jsonBody['query'] ?? '';
    }
} else {
    $query = $_GET['query'] ?? '';
}

if (empty($query)) {
    echo json_encode(generateResponse('error', 'Please enter something to search.', 
        ['suggestions' => getPopularSuggestions($pdo)]));
    exit();
}

// Detect user intent
$normalizedQuery = normalizeInput($query);
$intent = detectIntent($normalizedQuery);
$response = null;

switch ($intent['type']) {
    case 'greeting':
        $response = generateResponse('greeting', 
            "👋 Hello! I'm your AI Grocery Assistant.\n\nI can help you with:\n• Finding recipes for any dish\n• Getting ingredients list\n• Step-by-step cooking instructions\n• Veg/Non-veg recommendations\n\nWhat would you like to cook today?",
            ['suggestions' => getPopularSuggestions($pdo, 3)]);
        break;
        
    case 'farewell':
        $response = generateResponse('text', 
            "👋 Goodbye! Happy cooking! Come back anytime you need recipe help.");
        break;
        
    case 'thanks':
        $response = generateResponse('text', 
            "😊 You're welcome! Is there anything else I can help you with?",
            ['suggestions' => getPopularSuggestions($pdo, 3)]);
        break;
        
    case 'help':
        $response = generateResponse('help', 
            "🤖 Here's how I can help you:\n\n1️⃣ **Search recipes** - Just type a dish name like 'Biryani' or 'Pasta'\n\n2️⃣ **Browse by category** - Try 'Indian recipes' or 'Italian dishes'\n\n3️⃣ **Dietary preferences** - Ask for 'veg recipes' or 'non-veg dishes'\n\n4️⃣ **Quick options** - Say 'easy recipes' or 'random' for surprise picks\n\nTry these to get started:",
            ['suggestions' => getPopularSuggestions($pdo, 3)]);
        break;
        
    case 'random':
        $recipes = getRandomRecipes($pdo, 5);
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🎲 Here are some random picks for you:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('text', "Sorry, no recipes available.");
        }
        break;
        
    case 'veg':
        $recipes = getVegRecipes($pdo);
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🥗 I found " . count($recipes) . " vegetarian recipes for you:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No vegetarian recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'nonveg':
        $recipes = getNonVegRecipes($pdo);
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🍗 I found " . count($recipes) . " non-vegetarian recipes for you:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No non-vegetarian recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'easy':
        $recipes = getEasyRecipes($pdo);
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "👨‍🍳 Here are " . count($recipes) . " easy recipes perfect for beginners:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No easy recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'dessert':
        $recipes = getRecipesByCategory($pdo, 'Dessert');
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🍰 I found " . count($recipes) . " sweet dessert recipes:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No dessert recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'indian':
        $recipes = getRecipesByCategory($pdo, 'Indian');
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🇮🇳 Here are " . count($recipes) . " delicious Indian recipes:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No Indian recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'southindian':
        $recipes = getRecipesByCategory($pdo, 'South Indian');
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🌴 Here are " . count($recipes) . " authentic South Indian recipes:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No South Indian recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'italian':
        $recipes = getRecipesByCategory($pdo, 'Italian');
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🇮🇹 Here are " . count($recipes) . " Italian recipes:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No Italian recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'chinese':
        $recipes = getRecipesByCategory($pdo, 'Chinese');
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🥢 Here are " . count($recipes) . " Chinese recipes:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No Chinese recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'continental':
        $recipes = getRecipesByCategory($pdo, 'Continental');
        if (!empty($recipes)) {
            $response = generateResponse('multiple_matches', 
                "🍽️ Here are " . count($recipes) . " Continental recipes:",
                ['recipes' => createRecipeList($recipes)]);
        } else {
            $response = generateResponse('no_match', 
                "No Continental recipes found.",
                ['suggestions' => getPopularSuggestions($pdo)]);
        }
        break;
        
    case 'breakfast':
    case 'lunch':
    case 'dinner':
    case 'snack':
        $recipes = getRandomRecipes($pdo, 5);
        $mealType = ucfirst($intent['type']);
        $response = generateResponse('multiple_matches', 
            "🍽️ Here are some great options for $mealType:",
            ['recipes' => createRecipeList($recipes)]);
        break;
        
    case 'suggest':
        $recipes = getRandomRecipes($pdo, 5);
        $response = generateResponse('multiple_matches', 
            "💡 Here are my recommendations for you:",
            ['recipes' => createRecipeList($recipes)]);
        break;
        
    case 'how_to':
    case 'recipe_search':
    default:
        // Extract dish name and search
        $dishName = extractDishName($query);
        $keywords = extractKeywords(normalizeInput($dishName));
        $searchResult = searchRecipes($pdo, $keywords, normalizeInput($dishName));
        
        if (empty($searchResult['recipes'])) {
            $response = generateResponse('no_match', 
                "I couldn't find a recipe for '$query'. Try these popular dishes:",
                ['suggestions' => getPopularSuggestions($pdo)]);
        } elseif (count($searchResult['recipes']) == 1) {
            $recipe = $searchResult['recipes'][0];
            $response = generateResponse('recipe', 
                "Here's the recipe for " . $recipe['dish_name'] . "!",
                ['data' => formatRecipe($recipe)]);
        } else {
            $response = generateResponse('multiple_matches', 
                "I found " . count($searchResult['recipes']) . " recipes matching your search:",
                ['recipes' => createRecipeList($searchResult['recipes'])]);
        }
        break;
}

echo json_encode($response, JSON_PRETTY_PRINT);
?>
