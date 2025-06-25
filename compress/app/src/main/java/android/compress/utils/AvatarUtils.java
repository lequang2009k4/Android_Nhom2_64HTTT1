package android.compress.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import java.util.Random;

/**
 * Tiện ích để tạo avatar hình tròn với chữ cái đầu tiên của username
 */
public class AvatarUtils {
    
    // Mảng màu material design để làm avatar
    private static final int[] COLORS = new int[] {
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#673AB7"), // Deep Purple
        Color.parseColor("#3F51B5"), // Indigo
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#03A9F4"), // Light Blue
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#009688"), // Teal
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#8BC34A"), // Light Green
        Color.parseColor("#CDDC39"), // Lime
        Color.parseColor("#FFC107"), // Amber
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#FF5722"), // Deep Orange
        Color.parseColor("#795548"), // Brown
        Color.parseColor("#607D8B")  // Blue Grey
    };
    
    /**
     * Tạo avatar hình tròn với chữ cái đầu tiên từ username
     */
    public static TextDrawable createLetterAvatar(String username) {
        // Nếu không có username, tạo một chữ cái mặc định
        if (username == null || username.isEmpty()) {
            username = "?";
        }
        
        // Lấy chữ cái đầu tiên và chuyển thành chữ hoa
        final String letter = username.substring(0, 1).toUpperCase();
        
        // Chọn màu ngẫu nhiên từ mảng màu
        final int color = getRandomColor(username);
        
        return new TextDrawable(letter, color);
    }
    
    /**
     * Lấy màu ngẫu nhiên từ mảng màu dựa trên username
     * Cùng một username sẽ luôn cho cùng một màu
     */
    private static int getRandomColor(String username) {
        if (username == null || username.isEmpty()) {
            return COLORS[new Random().nextInt(COLORS.length)];
        }
        
        // Tính hash code từ username để có màu ổn định cho mỗi người dùng
        int hashCode = username.hashCode();
        int index = Math.abs(hashCode) % COLORS.length;
        return COLORS[index];
    }
    
    /**
     * Lớp TextDrawable để vẽ chữ cái trong hình tròn
     */
    public static class TextDrawable extends ShapeDrawable {
        private final String text;
        private final Paint textPaint;
        private final Paint backgroundPaint;
        private final int radius;
        
        public TextDrawable(String text, int color) {
            super(new OvalShape());
            this.text = text;
            
            this.backgroundPaint = new Paint();
            this.backgroundPaint.setColor(color);
            this.backgroundPaint.setAntiAlias(true);
            
            this.textPaint = new Paint();
            this.textPaint.setColor(Color.WHITE);
            this.textPaint.setAntiAlias(true);
            this.textPaint.setTextAlign(Paint.Align.CENTER);
            this.textPaint.setTextSize(30f);
            
            this.radius = 48; // Kích thước mặc định
            
            setBounds(0, 0, radius, radius);
        }

        @Override
        public void draw(Canvas canvas) {
            int width = getBounds().width();
            int height = getBounds().height();
            
            // Vẽ hình tròn
            canvas.drawCircle(width / 2f, height / 2f, Math.min(width, height) / 2f, backgroundPaint);
            
            // Vẽ chữ cái
            Rect textBounds = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), textBounds);
            
            float textHeight = textBounds.height();
            canvas.drawText(text, width / 2f, 
                    height / 2f + textHeight / 2f, textPaint);
        }

        @Override
        public int getIntrinsicWidth() {
            return radius * 2;
        }

        @Override
        public int getIntrinsicHeight() {
            return radius * 2;
        }
    }
} 