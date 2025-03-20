// Water Tracker Landing Page Animation
// Features: Slow-falling water particles

// Debug flag
console.log("Animation script loaded");

let waterDrops = [];     // Water drops falling from the sky

// Configuration
const NUM_WATER_DROPS = 25;  // Reduced number for less intensity

function setup() {
  console.log("Setup function called");
  // Create canvas and attach to container
  const canvas = createCanvas(windowWidth, windowHeight);
  console.log("Canvas created with dimensions:", windowWidth, "x", windowHeight);
  canvas.parent('animation-container');
  console.log("Canvas attached to animation-container");
  
  // Set initial background
  background(0, 43, 91); // Dark blue background
  
  // Initialize water drops
  createWaterDrops();
  
  console.log("Setup complete");
}

function draw() {
  // Semi-transparent background for trail effect
  background(0, 43, 91, 30);
  
  // Draw and update water drops
  updateAndDrawWaterDrops();
}

function windowResized() {
  console.log("Window resized");
  // Resize canvas when window size changes
  resizeCanvas(windowWidth, windowHeight);
}

// ===== INITIALIZATION FUNCTIONS =====

function createWaterDrops() {
  console.log("Creating water drops");
  waterDrops = [];
  for (let i = 0; i < NUM_WATER_DROPS; i++) {
    waterDrops.push({
      x: random(width),
      y: random(-500, -10),
      length: random(4, 12),        // Smaller drops
      speed: random(0.8, 2.2),      // Very slow falling speed
      thickness: random(0.5, 1.8),  // Thinner drops
      opacity: random(80, 150)      // Lower opacity
    });
  }
  console.log("Water drops created");
}

// ===== UPDATE AND DRAW FUNCTIONS =====

function updateAndDrawWaterDrops() {
  for (let drop of waterDrops) {
    // Draw the drop
    stroke(100, 181, 246, drop.opacity);
    strokeWeight(drop.thickness);
    line(drop.x, drop.y, drop.x, drop.y + drop.length);
    
    // Add a small ellipse at the bottom for a droplet effect
    noStroke();
    fill(100, 181, 246, drop.opacity);
    ellipse(drop.x, drop.y + drop.length, drop.thickness * 2);
    
    // Move the drop
    drop.y += drop.speed;
    
    // Reset drop if it goes off screen
    if (drop.y > height) {
      drop.y = random(-200, -50);
      drop.x = random(width);
    }
  }
}
