package com.example.walls;

import java.util.ArrayList;

import com.example.walls.GameActivity.DrawView.pointsView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameActivity extends Activity {
	
	Canvas canvas;
	Paint paint;
	Point Size;

	int distance = 70;
	int align = 0;

	int cardPlayerX = 20;
	int cardPlayerY = 900;
	int cardCpuX = 20;
	int cardCpuY = 30;
	int cardWidth = 120;
	int cardHeight = 200;

	float rx = 45;
	float ry = 45;

	int textSize = 50;
	int textX = 10;
	int textY = 60;
	int textX2 = 50;
	int textY2 = 120;

	RectF[] rects;
	Game game;

	boolean CardTouched = false;

	int beginCardCount = 5;
	int beginWallValue = 100;

	int hpPlayerX = 70;
	int hpPlayerY = 830;
	int hpCpuX = 70;
	int hpCpuY = 260;
	int hpWidth = 400;
	int hpHeight = 20;

	int WallX = 30;
	int WallY = 230;
	float WallScaleX = 0.9f;
	float WallScaleY = 1.25f;

	ArrayList<pointsView> dmg;
	int beginPointsCpuX = 200;
	int beginPointsCpuY = 400;
	int beginPointsPlayerX = 200;
	int beginPointsPlayerY = 600;
	int dmgColor = Color.rgb(255, 0, 0);
	int healColor = Color.BLUE;

	int borderColor = Color.rgb(25, 70, 15);
	int textColor = Color.rgb(255, 0, 0);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new DrawView(this));
		game = new Game(beginCardCount, beginWallValue);
		rects = new RectF[game.StartCardsCount];
		dmg = new ArrayList<pointsView>();

		try {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			Size = new Point(metrics.widthPixels, metrics.heightPixels);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	class DrawView extends SurfaceView implements SurfaceHolder.Callback,
			View.OnTouchListener {

		private DrawThread drawThread;

		public DrawView(Context context) {
			super(context);
			setOnTouchListener(this);
			getHolder().addCallback(this);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			drawThread = new DrawThread(getHolder());
			drawThread.setRunning(true);
			drawThread.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			boolean retry = true;
			drawThread.setRunning(false);
			while (retry) {
				try {
					drawThread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
		}

		class DrawThread extends Thread {

			private boolean running = false;
			private SurfaceHolder surfaceHolder;

			public DrawThread(SurfaceHolder surfaceHolder) {
				this.surfaceHolder = surfaceHolder;
				dmg.add(new pointsView(0, 0, 0, 0));
			}

			public void setRunning(boolean running) {
				this.running = running;
			}

			@Override
			public void run() { //Метод обновляет canvas
				while (running) {
					canvas = null;
					try {
						canvas = surfaceHolder.lockCanvas(null);
						if (canvas == null)
							continue;
						canvas.drawColor(Color.GREEN);
						Bitmap img = BitmapFactory.decodeResource(
								getResources(), R.drawable.grass);
						canvas.scale(1f, 1f);
						img = Bitmap.createScaledBitmap(img, Size.x, Size.y,
								false);
						canvas.drawBitmap(img, 0, 0, paint);
						img.recycle();
						if (game.CardsPlayerN.length > 0) {
							drawCards(1, cardPlayerX, cardPlayerY);
						}
						if (game.CardsCpuN.length > 0) {
							drawCards(0, cardCpuX, cardCpuY);
						}

						drawWallHP(hpPlayerX, hpPlayerY, hpWidth, hpHeight,
								game.WallPlayer);

						drawWallHP(hpCpuX, hpCpuY, hpWidth, hpHeight,
								game.WallCpu);
						drawWalls();
						for (int i = dmg.size() - 1; i > 0; i--) {
							if (dmg.get(i).Draw() == -1) {
								dmg.remove(i);
							}
						}
						if (game.GameResult == 1) {
							textOut("CPU WIN!!!", 200, 500, borderColor,
									textColor);
						}
						if (game.GameResult == 2) {
							textOut("PLAYER WIN!!!", 200, 500, borderColor,
									textColor);
						}
					} catch (Throwable e) {
						e.printStackTrace();
						textOut(e.getMessage(), 30, 650, borderColor, textColor);
					} finally {
						if (canvas != null) {
							surfaceHolder.unlockCanvasAndPost(canvas);
						}
					}
				}
			}
			//метод реализует рисование стен
			public void drawWalls() {
				paint = new Paint();
				paint.setAntiAlias(true);
				paint.setStrokeWidth(1);
				paint.setStyle(Paint.Style.FILL);
				Bitmap img1 = BitmapFactory.decodeResource(getResources(),
						R.drawable.wall1);
				canvas.scale(WallScaleX, WallScaleY);
				canvas.drawBitmap(img1, WallX, WallY, paint);
				img1.recycle();
			}
			//метод реализует рисование карт
			public void drawCards(int numberPlayer, int x, int y) {
				paint = new Paint();
				paint.setAntiAlias(true);
				paint.setColor(Color.YELLOW);
				paint.setStrokeWidth(5);
				paint.setStyle(Paint.Style.FILL);
				int length;
				float scale = 1f;
				if (numberPlayer == 1) {
					length = game.CardsPlayerN.length;
				} else {
					length = game.CardsCpuN.length;
					scale = 1f;
				}
				distance = (Size.x - 4 * x) / length;
				if (distance > cardWidth + 20) {
					distance = cardWidth + 20;
					align = (int) (Size.x - cardWidth - distance * (length - 1) + x)
							/ 2 - x;
				}
				int shift = 0;
				for (int i = 0; i < length; i++) {
					if (game.SelectedCard == i && numberPlayer == 1)
						shift = 40;
					rects[i] = new RectF(x + distance * i + align, y - shift,
							(x + cardWidth + distance * i + align) / scale, (y
									+ cardHeight - shift)
									/ scale);
					paint.setColor(Color.YELLOW);
					paint.setStyle(Paint.Style.FILL);
					canvas.drawRoundRect(rects[i], rx, ry, paint);
					paint.setStyle(Paint.Style.STROKE);
					paint.setColor(Color.BLACK);
					canvas.drawRoundRect(rects[i], rx, ry, paint);
					if (numberPlayer == 1) {
						textOut(Integer.toString(game.CardsPlayerN[i]), x
								+ textX + distance * i + align, y + textY
								- shift, borderColor, dmgColor);
					}
					paint.setColor(Color.BLUE);
					if (numberPlayer == 1) {

						textOut(Integer.toString(game.CardsPlayerP[i]), x
								+ textX + distance * i + align + textX2, y
								+ textY + textY2 - shift, borderColor,
								healColor);
					}
					shift = 0;
				}
			}
			//метод реализует рисование хп стен
			public void drawWallHP(int x, int y, int width, int height,
					int valueHP) {
				paint = new Paint();
				paint.setAntiAlias(true);
				paint.setStrokeWidth(5);
				paint.setColor(Color.RED);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawRoundRect(new RectF(x, y, x + width * valueHP
						/ beginWallValue, y + height), rx, ry, paint);

				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.BLACK);
				canvas.drawRoundRect(new RectF(x, y, x + width, y + height),
						rx, ry, paint);

				textOut(Integer.toString(valueHP), x + width + 10, y + height,
						borderColor, textColor);
			}

		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {//обработка нажатия на экран
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if(game.GameResult!=0) {
					//если результат игры победа, то инициализируем хп стен и результат 
					game.WallCpu=game.WallPlayer=beginWallValue;
					game.GameResult=0;
				}
				int tempSelectedCard = -1;
				tempSelectedCard = onClickCard(event.getX(), event.getY());
				if (tempSelectedCard != -1 && tempSelectedCard < rects.length) {
					if (tempSelectedCard == game.SelectedCard) {
						dmg.add(new pointsView(
								game.CardsPlayerP[tempSelectedCard],
								beginPointsPlayerX, beginPointsPlayerY,
								healColor));
						dmg.add(new pointsView(
								game.CardsPlayerN[tempSelectedCard],
								beginPointsCpuX, beginPointsCpuY, dmgColor));
						game.movePlayer(tempSelectedCard);

						game.moveCpu();
						game.RegenerateCards();
						dmg.add(new pointsView(game.CpuCardP,
								beginPointsCpuX + 300, beginPointsCpuY + 10,
								healColor));
						dmg.add(new pointsView(game.CpuCardN,
								beginPointsPlayerX + 300,
								beginPointsPlayerY + 10, dmgColor));

						game.SelectedCard = -1;
					} else {
						game.SelectedCard = tempSelectedCard;
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			}
			return false;
		}
		//Метод возвращает номер нажатой карты или -1
		public int onClickCard(float X, float Y) {
			if (game.CardsPlayerN.length > 0) {
				if (Y > cardPlayerY && Y < cardPlayerY + cardHeight) {
					if (X > cardPlayerX
							&& X < rects[game.CardsPlayerN.length - 1].right) {
						int tempSelectedCard = (int) (X - cardPlayerX - align)
								/ distance;
						if (distance > cardWidth) {
							if (rects[tempSelectedCard].left < X
									&& rects[tempSelectedCard].right > X) {
								return tempSelectedCard;
							} else {
								return -1;
							}
						} else {
							if (tempSelectedCard >= game.CardsPlayerN.length - 1) {
								tempSelectedCard = game.CardsPlayerN.length - 1;
								if (rects[tempSelectedCard].left < X
										&& rects[tempSelectedCard].right > X) {
									return tempSelectedCard;
								} else {
									return -1;
								}
							} else {
								return tempSelectedCard;
							}
						}
					}
				} else {
					game.SelectedCard = -1;
				}
			}
			return -1;
		}
		//метод выводит текст на экран
		public void textOut(String text, int x, int y, int ColorBack,
				int ColorFront) {

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStrokeWidth(5);
			paint.setTextSize(textSize);
			paint.setColor(ColorBack);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawText(text, x, y, paint);
			paint.setColor(ColorFront);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawText(text, x, y, paint);
		}
		
		//класс реализует рисование очков урона и востановления
		class pointsView {
			private int damage;
			private int beginX;
			private int beginY;
			private int color;
			private int x;
			private int y;
			private Boolean isInc;

			public pointsView(int Damage, int BeginX, int BeginY, int color) {
				this.damage = Damage;
				this.x = this.beginX = BeginX;
				this.y = this.beginY = BeginY;
				this.color = color;
				isInc = true;
			}

			public int Draw() {
				if (x > beginX + 20) {
					isInc = false;
				}
				if (x < beginX - 20) {
					isInc = true;
				}
				x = isInc ? x + 5 : x - 5;
				y -= 3;
				if (y < beginY - 100) {
					return -1;
				}
				textOut(Integer.toString(damage), x, y, Color.BLACK, color);
				return 0;
			}
		}
	}
}
