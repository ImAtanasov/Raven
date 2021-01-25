package com.example.peach;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    public static final int BUTTON_WIDTH = 200;
    @SuppressLint("StaticFieldLeak")
    private static Context _context;
    private RecyclerView recyclerView;
    private List<UnderlayButton> buttons;
    private GestureDetector gestureDetector;
    private int swipedPos = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<UnderlayButton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;
    List<Profile> searchList;
    int is_active = 0;

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (UnderlayButton button : buttons) {
                if (button.onClick(e.getX(), e.getY()))
                    break;
            }
            return true;
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            if (swipedPos < 0) return false;
            Point point = new Point((int) e.getRawX(), (int) e.getRawY());

            RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
            View swipedItem = null;
            if (swipedViewHolder != null) {
                swipedItem = swipedViewHolder.itemView;
            }
            Rect rect = new Rect();
            if (swipedItem != null) {
                swipedItem.getGlobalVisibleRect(rect);
            }

            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y) {
                    gestureDetector.onTouchEvent(e);

                } else {

                    recoverQueue.add(swipedPos);
                    swipedPos = -1;
                    recoverSwipedItem();
                }
            }
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public SwipeHelper(Context context, RecyclerView recyclerView, List<Profile> searchList) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this._context = context;
        this.recyclerView = recyclerView;
        this.searchList = searchList;
        this.buttons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.recyclerView.setOnTouchListener(onTouchListener);
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };

        attachSwipe();
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos)
            recoverQueue.add(swipedPos);

        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        swipeThreshold = 0.5f * buttons.size() * BUTTON_WIDTH;
        recoverSwipedItem();
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;


        //Toast.makeText(_context, "qqqq " + isCurrentlyActive, Toast.LENGTH_SHORT).show();




        if (pos < 0) {
            swipedPos = pos;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX > 0) {
                is_active = 0;
                List<UnderlayButton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                } else {
                    buffer = buttonsBuffer.get(pos);
                }

                translationX = dX * buffer.size() * BUTTON_WIDTH / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            } else if (dX == 0) {
                is_active = 0;
            } else {
                if (dX < 1) {
                    if (is_active != 1) {
                        Intent intent = new Intent(_context, ChatWithExtendable.class);
                        intent.putExtra("friend_id", searchList.get(pos).getUserID());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        _context.startActivity(intent);
                        searchList.get(pos).setLamp(0);
                        recyclerView.getAdapter().notifyItemChanged(pos);
                        is_active = 1;
                        //start simulating press
                        long downTime = SystemClock.uptimeMillis();
                        long eventTime = SystemClock.uptimeMillis();
                        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[1];
                        MotionEvent.PointerProperties pp1 = new MotionEvent.PointerProperties();
                        pp1.id = 0;
                        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
                        properties[0] = pp1;
                        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
                        MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
                        pc1.x = dX;
                        pc1.y = dY;
                        pc1.pressure = 1;
                        pc1.size = 1;
                        pointerCoords[0] = pc1;
                        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime,
                                MotionEvent.ACTION_DOWN, 1, properties,
                                pointerCoords, 0,  0, 1, 1, 0, 0, 0, 0 );
                        recyclerView.dispatchTouchEvent(motionEvent);

                        motionEvent = MotionEvent.obtain(downTime, eventTime,
                                MotionEvent.ACTION_UP, 1, properties,
                                pointerCoords, 0,  0, 1, 1, 0, 0, 0, 0 );
                        recyclerView.dispatchTouchEvent(motionEvent);
                        //end simulating press
                    }

                    /*new Thread(() -> {
                        try {
                            Looper.prepare();
                            Instrumentation inst = new Instrumentation();
                            Thread.sleep(1000);
                            inst.sendKeyDownUpSync(KeyEvent.ACTION_DOWN);
                        } catch (InterruptedException e) {
                        }
                    }).start(); */

                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            int pos = recoverQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }
    }

    private void drawButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX) {
        float right = itemView.getLeft();
        float dButtonWidth = (-1) * dX / buffer.size();

        for (UnderlayButton button : buffer) {
            float left = right - dButtonWidth;
            button.onDraw(
                    c,
                    new RectF(
                            right,
                            itemView.getTop(),
                            left,
                            itemView.getBottom()
                    ),
                    pos
            );

            right = left;
        }
    }

    public void attachSwipe() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);

    public static class UnderlayButton {
        private String text;
        private int imageResId;
        private int color;
        private int pos;
        private RectF clickRegion;
        private UnderlayButtonClickListener clickListener;

        public UnderlayButton(String text, int imageResId, int color, UnderlayButtonClickListener clickListener) {
            this.text = text;
            this.imageResId = imageResId;
            this.color = color;
            this.clickListener = clickListener;
        }

        public boolean onClick(float x, float y) {
            if (clickRegion != null && clickRegion.contains(x, y)) {
                clickListener.onClick(pos);
                return true;
            }

            return false;
        }

        @SuppressLint("ResourceAsColor")
        public void onDraw(Canvas c, RectF rect, int pos) {
            Paint p = new Paint();

            // Draw background
            p.setColor(color);
            c.drawRect(rect, p);

            // Draw Text
            p.setColor(Color.CYAN);
            p.setTextSize(50);

            Rect r = new Rect();
            float cHeight = rect.height();
            float cWidth = rect.width();
            p.setTextAlign(Paint.Align.LEFT);
            p.getTextBounds(text, 0, text.length(), r);
            float x = cWidth / 2f - r.width() / 2f - r.left;
            float y = cHeight / 2f + r.height() / 2f - r.bottom;
            c.drawText(text, rect.left + x, rect.top + y, p);

            clickRegion = rect;
            this.pos = pos;
        }
    }

    public interface UnderlayButtonClickListener {
        void onClick(int pos);
    }
}