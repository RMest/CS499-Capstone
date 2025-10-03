#include <GLFW\glfw3.h>
#include <iostream>
#include <vector>

using namespace std;

const float DEG2RAD = 3.14159 / 180;

void processInput(GLFWwindow* window);

enum BRICKTYPE { REFLECTIVE, DESTRUCTABLE };
enum ONOFF { ON, OFF };

class Brick
{
public:
	float red, green, blue;
	float x, y, width;
	BRICKTYPE brick_type;
	ONOFF onoff;
	int hitPoints;
	int maxHitPoints;

	Brick(BRICKTYPE bt, float xx, float yy, float ww, float rr, float gg, float bb, int hp = 1)
	{
		brick_type = bt; x = xx; y = yy, width = ww; red = rr, green = gg, blue = bb;
		onoff = ON;
		hitPoints = hp;
		maxHitPoints = hp;
	};

	void drawBrick()
	{
		if (onoff == ON)
		{
			double halfside = width / 2;

			if (brick_type == DESTRUCTABLE) {
				float hp = (float)hitPoints / maxHitPoints;
				float r = 1.0f - hp;
				float g = hp;
				float b = 0.0f;

				glColor3f(r, g, b);
			}
			else {
				glColor3d(red, green, blue);
			}
			glBegin(GL_POLYGON);

			glVertex2d(x + halfside, y + halfside);
			glVertex2d(x + halfside, y - halfside);
			glVertex2d(x - halfside, y - halfside);
			glVertex2d(x - halfside, y + halfside);

			glEnd();
		}
	}
};


class Circle
{
public:
	float red, green, blue;
	float radius;
	float x;
	float y;
	float speed = 0.007;
	int direction; // 1=up 2=right 3=down 4=left 5=up right  6=up left  7=down right  8=down left
	bool collision = false;

	Circle(double xx, double yy, double rr, int dir, float rad, float r, float g, float b)
	{
		x = xx;
		y = yy;
		radius = rr;
		red = r;
		green = g;
		blue = b;
		radius = rad;
		direction = dir;
	}

	bool CheckBallCollision(const Circle& other) // ???? why is this here ?????
	{
		float dx = x - other.x;  
		float dy = y - other.y;
		float distanceSquared = dx * dx + dy * dy;
		float radiusSum = radius + other.radius;

		return distanceSquared < (radiusSum * radiusSum);
	} 

	void CheckBrickCollision(Brick* brk)
	{
		if (brk->onoff == OFF) return;

		if (brk->brick_type == REFLECTIVE)
		{
			if ((x > brk->x - brk->width && x <= brk->x + brk->width) && (y > brk->y - brk->width && y <= brk->y + brk->width))
			{
				BounceFromPoint(brk->x, brk->y);
			}
		}
		else if (brk->brick_type == DESTRUCTABLE)
		{
			if ((x > brk->x - brk->width && x <= brk->x + brk->width) && (y > brk->y - brk->width && y <= brk->y + brk->width))
			{
				brk->hitPoints--;
				BounceFromPoint(brk->x, brk->y);

				if (brk->hitPoints <= 0) 
				{
					brk->onoff = OFF;
				}
			}
		}
	}

	void BounceFromPoint(float targetX, float targetY)
	{
		float dx = x - targetX;
		float dy = y - targetY;

		if (abs(dx) > abs(dy)) {
			// Horizontal hit
			if (direction == 2) direction = 4;
			else if (direction == 4) direction = 2;
			else if (direction == 5) direction = 6;
			else if (direction == 6) direction = 5;
			else if (direction == 7) direction = 8;
			else if (direction == 8) direction = 7;
		}
		else {
			// Vertical hit
			if (direction == 1) direction = 3;
			else if (direction == 3) direction = 1;
			else if (direction == 5) direction = 7;
			else if (direction == 6) direction = 8;
			else if (direction == 7) direction = 5;
			else if (direction == 8) direction = 6;
		}
	}

	void MoveOneStep()
	{
		if (direction == 1 || direction == 5 || direction == 6)  // up
		{
			if (y > -1 + radius) {
				y -= speed;
			}
			else
			{
				if (direction == 1) direction = 3;
				else if (direction == 5) direction = 7;
				else if (direction == 6) direction = 8;
				return;
			}
		}

		if (direction == 2 || direction == 5 || direction == 7)  // right
		{
			if (x < 1 - radius) {
				x += speed;
			}
			else
			{
				if (direction == 2) direction = 4;
				else if (direction == 5) direction = 6;
				else if (direction == 7) direction = 8;
				return;
			}
		}

		if (direction == 3 || direction == 7 || direction == 8)  // down
		{
			if (y < 1 - radius) {
				y += speed;
			}
			else
			{
				if (direction == 3) direction = 1;
				else if (direction == 7) direction = 5;
				else if (direction == 8) direction = 6;
				return;
			}
		}

		if (direction == 4 || direction == 6 || direction == 8)  // left
		{
			if (x > -1 + radius) {
				x -= speed;
			}
			else
			{
				if (direction == 4) direction = 2;
				else if (direction == 6) direction = 5;
				else if (direction == 8) direction = 7;
				return;
			}
		}
	}

	void DrawCircle()
	{
		glColor3f(red, green, blue);
		glBegin(GL_POLYGON);
		for (int i = 0; i < 360; i++) {
			float degInRad = i * DEG2RAD;
			glVertex2f((cos(degInRad) * radius) + x, (sin(degInRad) * radius) + y);
		}
		glEnd();
	}
};

vector<Circle> world;


int main(void) {
	srand(time(NULL));

	if (!glfwInit()) {
		exit(EXIT_FAILURE);
	}
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
	GLFWwindow* window = glfwCreateWindow(720, 720, "8-2 Assignment", NULL, NULL);
	if (!window) {
		glfwTerminate();
		exit(EXIT_FAILURE);
	}
	glfwMakeContextCurrent(window);
	glfwSwapInterval(1);

	//changed to a vector to allows easy scalability
	vector<Brick> bricks = {
		//Destructable bricks
		Brick(DESTRUCTABLE, -0.6, -0.2, 0.2, 0, 0, 0, 100), // X cord, Y cord, size, R, G, B, HP
		Brick(DESTRUCTABLE, -0.4, -0.4, 0.2, 0, 0, 0, 100), // HP only applicable to destructable blocks.
		Brick(DESTRUCTABLE, -0.2, -0.5, 0.2, 0, 0, 0, 100), // RGB only applicable to reflective blocks.
		Brick(DESTRUCTABLE, 0, -0.55, 0.2, 0, 0, 0, 100),
		Brick(DESTRUCTABLE, 0.2, -0.5, 0.2, 0, 0, 0, 100),
		Brick(DESTRUCTABLE, 0.4, -0.4, 0.2, 0, 0, 0, 100),
		Brick(DESTRUCTABLE, 0.6, -0.2, 0.2, 0, 0, 0, 100),
		//Reflective Bricks
		Brick(REFLECTIVE, 0.5, 0.33, 0.2, 1, 0.4, 1),
		Brick(REFLECTIVE, -0.5, 0.33, 0.2, 1, 0.4, 1),
		Brick(REFLECTIVE, 0, 0, 0.2, 1, 0.4, 1),
	};

	cout << "Controls:\nSpace Bar - Spawn one ball\nLeft Shift - Spawn many balls\nEsc - Close application\n";
	

	while (!glfwWindowShouldClose(window)) {
		//Setup View
		float ratio;
		int width, height;
		glfwGetFramebufferSize(window, &width, &height);
		ratio = width / (float)height;
		glViewport(0, 0, width, height);
		glClear(GL_COLOR_BUFFER_BIT);

		processInput(window);

		//Movement
		for (int i = 0; i < world.size(); i++)
		{
			//added for loop to collision to prevent repetitive code
			for (auto& brick : bricks) {
				world[i].CheckBrickCollision(&brick);
			}
			world[i].MoveOneStep();
			world[i].DrawCircle();

		}

		//Handles balls bouncing off each other
		for (size_t i = 0; i < world.size(); ++i) {
			for (size_t j = i + 1; j < world.size(); ++j) {
				Circle& a = world[i];
				Circle& b = world[j];

				float dx = a.x - b.x;
				float dy = a.y - b.y;
				float distance = sqrt(dx * dx + dy * dy);
				float minDist = a.radius + b.radius;

				if (distance < minDist && distance > 0.0f) {
					// Bounce logic
					a.BounceFromPoint(b.x, b.y);
					b.BounceFromPoint(a.x, a.y);

					// Separate overlapping circles
					float overlap = minDist - distance;

					// Normalize direction
					float nx = dx / distance;
					float ny = dy / distance;

					// Displace both circles equally
					a.x += nx * (overlap / 2.0f);
					a.y += ny * (overlap / 2.0f);
					b.x -= nx * (overlap / 2.0f);
					b.y -= ny * (overlap / 2.0f);
				}
			}
		}

		//added for loop to draw bricks to prevent repetitive code
		for (auto& brick : bricks) {
			brick.drawBrick();
		}

		glfwSwapBuffers(window);
		glfwPollEvents();
	}

	glfwDestroyWindow(window);
	glfwTerminate;
	exit(EXIT_SUCCESS);
}

// If I only want a specific button to play ONCE
int playOnce = 0;

void spawnBall() {
	cout << RAND_MAX << endl;
	float xStart = ((float)rand() / RAND_MAX) * 2.0f - 1.0f;
	int randDir = (rand() % 8) + 1;
	double r, g, b;
	r = rand() / 10000;
	g = rand() / 10000;
	b = rand() / 10000;
	Circle B(xStart, -0.9, 2, randDir, 0.05, r, g, b);
	world.push_back(B);
}

void processInput(GLFWwindow* window)
{
	
	if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
		glfwSetWindowShouldClose(window, true);

	if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS)
	{
		if (playOnce == 0)
		{
			spawnBall();
			playOnce = 1;
		}
	}

	if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_RELEASE)
	{
		playOnce = 0;
	}

	if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)
	{
		spawnBall();
	}
}