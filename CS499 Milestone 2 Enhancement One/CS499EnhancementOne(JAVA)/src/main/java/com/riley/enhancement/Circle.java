package com.riley.enhancement;

import static java.lang.Math.*;

import static org.lwjgl.opengl.GL11.*;

public class Circle {
	double DEG2RAD = PI / 180;
	float red, green, blue;
	double radius;
	double x, y;
	double vx, vy;
	double speed = 0.007;
	int direction; // 1=up 2=right 3=down 4=left 5=up right  6=up left  7=down right  8=down left

	public Circle(double xx, double yy, double rr, double angle, float r, float g, float b) {
		this.x = xx;
		this.y = yy;
		this.radius = rr;
		this.red = r;
		this.green = g;
		this.blue = b;
		
		this.vx = speed * cos(angle);
		this.vy = speed * sin(angle);
	}
	
	void drawCircle()
	{
		glColor3f(red, green, blue);
		glBegin(GL_POLYGON);
		for (int i = 0; i < 360; i++) {
			double degInRad = i * DEG2RAD;
		    float vx = (float) (Math.cos(degInRad) * radius + x);
	        float vy = (float) (Math.sin(degInRad) * radius + y);
	        glVertex2f(vx, vy);
	    }
	    glEnd();
	}
	
	// Checks if circles are on or overlapping a brick
	void CheckBrickCollision(Brick brk) {
		if (brk.onoff == ONOFF.OFF) return;

		if ((x > brk.x - (brk.width * 0.75) && x <= brk.x + (brk.width * 0.75)) && 
			(y > brk.y - (brk.width * 0.75) && y <= brk.y + (brk.width * 0.75))) {
			
			BounceFromPoint(brk.x, brk.y);
			
			if (brk.brick_type == BRICKTYPE.DESTRUCTABLE) {
				brk.hitPoints--;
				if (brk.hitPoints <= 0) {
					brk.onoff = ONOFF.OFF;
				}
			}
		}
	}
	
	// Bounces ball off of bricks
	void BounceFromPoint(double targetX, double targetY) {
		double dx = x - targetX;
		double dy = y - targetY;

		if (abs(dx) > abs(dy)) {
			vx = -vx;
			if (dx > 0) {
			}
		} else {
			vy = -vy;
			if (dy > 0) {
			}
		}
	}
	
	// Continues movement of balls and keeps balls ons screen without leaving scene
	void MoveOneStep() {
		x += vx;
		y += vy;
		
		double xmin = -1 + radius, xmax = 1 - radius;
		double ymin = -1 + radius, ymax = 1 - radius;
		
		if (x < xmin || x > xmax) {
			vx = -vx;
			x = max(min(x, xmax), xmin);
		}
		if (y < ymin || y > ymax) {
			vy = -vy;
			y = max(min(y, ymax), ymin);
		}
	}
}
