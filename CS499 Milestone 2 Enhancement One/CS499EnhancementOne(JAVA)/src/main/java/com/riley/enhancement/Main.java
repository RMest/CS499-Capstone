package com.riley.enhancement;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
	
	// window handle
	private long window;
	
	boolean spawnOneCircle = true;
	
	boolean spawnMany = true;
	
	// ArrayList to hold all bricks
	ArrayList<Brick> bricks = new ArrayList<>();
	ArrayList<Circle> circles = new ArrayList<>();
	
	public void run() {
		
		init();
		loop();
		
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
			
	}
	
	private void init() {
		
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();
		
		if (!glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");
		
		// Configure GLFW
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
		
		// Create window
		window = glfwCreateWindow(720, 720, "Enhancement One", NULL, NULL);
		if(window == NULL) {
			throw new RuntimeException("Failed to create GLFW Window");
		}
		
		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
				
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
		
		// Adds bricks to arrayList                   X, Y, Size, Red, Green, Blue, HP
		bricks.add(new Brick(BRICKTYPE.DESTRUCTABLE, -0.6, -0.2, 0.2, 0f, 0f, 0f, 100));
		bricks.add(new Brick(BRICKTYPE.DESTRUCTABLE, -0.4, -0.4, 0.2, 0f, 0f, 0f, 100));
		bricks.add(new Brick(BRICKTYPE.DESTRUCTABLE, -0.2, -0.5, 0.2, 0f, 0f, 0f, 100));
		bricks.add(new Brick(BRICKTYPE.DESTRUCTABLE, 0.0, -0.55, 0.2, 0f, 0f, 0f, 100));
		bricks.add(new Brick(BRICKTYPE.DESTRUCTABLE, 0.2, -0.5, 0.2, 0f, 0f, 0f, 100));
		bricks.add(new Brick(BRICKTYPE.DESTRUCTABLE, 0.4, -0.4, 0.2, 0f, 0f, 0f, 100));
		bricks.add(new Brick(BRICKTYPE.DESTRUCTABLE, 0.6, -0.2, 0.2, 0f, 0f, 0f, 100));
		bricks.add(new Brick(BRICKTYPE.REFLECTIVE, 0.5, 0.33, 0.2, 1.0f, 0.4f, 1.0f, 1));
		bricks.add(new Brick(BRICKTYPE.REFLECTIVE, -0.5, 0.33, 0.2, 1.0f, 0.4f, 1.0f, 1));
		bricks.add(new Brick(BRICKTYPE.REFLECTIVE, 0.0, 0.0, 0.2, 1.0f, 0.4f, 1.0f, 1));
		
		// key callback. Is called every time a key is pressed.
		glfwSetKeyCallback(window, (window, key, _scancode, action, _mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			
			if (key == GLFW_KEY_SPACE && action == GLFW_PRESS)
				spawnCircle();
				
			if (key == GLFW_KEY_LEFT_SHIFT && action == GLFW_REPEAT)
				spawnCircle();
		});
		
	}
	
	private void loop() {
		GL.createCapabilities();

		// Run the rendering loop until user clicks esc or closes window
		while( !glfwWindowShouldClose(window) ) {
			
			glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer
			
			for(Brick brick: bricks) {
				brick.drawBrick(); // draws all bricks in brick array
			}
				
			for(Circle circle: circles) { 
				circle.drawCircle(); // draws all circles in circle array
				circle.MoveOneStep(); // applies movement to circles
				CheckBallCollision();
				
				for(Brick brick: bricks) { 
					circle.CheckBrickCollision(brick); // checks all collision from circles to bricks
				}
			}

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}
	
	void spawnCircle() {
		Random random = new Random();
		float xStart = ((float)random.nextInt(10000) / 10000) * 2.0f - 1.0f;
		double randDir = (random.nextDouble(360));
		float r, g, b;
		r = random.nextFloat(10000) / 10000;
		g = random.nextFloat(10000) / 10000;
		b = random.nextFloat(10000) / 10000;
		circles.add(new Circle(xStart, -0.9, 0.05, randDir, r, g, b));
	}
	
	void CheckBallCollision() {
		//Handles balls bouncing off each other
		for (Circle i: circles) {
			for (Circle j: circles) {
	
				double dx = i.x - j.x;
				double dy = i.y - j.y;
				double distance = Math.sqrt(dx * dx + dy * dy);
				double minDist = i.radius + j.radius;
	
				if (distance < minDist && distance > 0.0f) {
					// Bounce logic
					i.BounceFromPoint(j.x, j.y);
					j.BounceFromPoint(i.x, i.y);
	
					// Separate overlapping circles
					double overlap = minDist - distance;
	
					// Normalize direction
					double nx = dx / distance;
					double ny = dy / distance;
	
					// Displace both circles equally
					i.x += nx * (overlap / 2.0f);
					i.y += ny * (overlap / 2.0f);
					j.x -= nx * (overlap / 2.0f);
					j.y -= ny * (overlap / 2.0f);
				}
			}
		}
	}
	
	public static void main(String[] arg) {
		new Main().run();
	}
}
