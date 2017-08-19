package com.mwiti.collins.inspector;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by collins on 8/19/17.
 */

//class InspectorUtil
public final class InspectorUtil {
    //to throw exception if there is no instances
    private InspectorUtil() {
        throw new UnsupportedOperationException("No instances");
    }

    /**
     * @param context
     * @param data
     * @return
     */
    // I have overridden with annotation nullable in order to allow the value to be set to the special value NULL instead of the usual possible values of the data type.
    // used to retrieve selected image
    @Nullable
    public static byte[] retrieveSelectedImage(@NonNull Context context, @NonNull Intent data) {
        InputStream inStream = null;
        Bitmap bitmap = null;
        try {
            inStream = context.getContentResolver().openInputStream(data.getData());
            bitmap = BitmapFactory.decodeStream(inStream);
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            return outStream.toByteArray();
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ignored) {
                }
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    //I have used nonnull annotation to prevent null pointer exception from taking place
    @NonNull
    public static Activity unwrapActivity(@NonNull Context startFrom) {
        while (startFrom instanceof ContextWrapper) {
            if (startFrom instanceof Activity) {
                return ((Activity) startFrom);
            }
            startFrom = ((ContextWrapper) startFrom).getBaseContext();
        }
        throw new IllegalStateException("The Context cannot be unwrapped to an Activity!");
    }

    //I have used T which is used for parameterization. It makes use of Java Generics to produce parametized classes
    @Nullable
    public static <T> T firstChildOfType(@NonNull View root, @NonNull Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        //loop to enable child count. If not null, it should return the child result(image details), if there is null, it should display nothing
        if (root instanceof ViewGroup) {
            final ViewGroup rootGroup = (ViewGroup) root;
            for (int i = 0; i < rootGroup.getChildCount(); i++) {
                final View child = rootGroup.getChildAt(i);
                final T childResult = firstChildOfType(child, type);
                if (childResult != null) {
                    return childResult;
                }
            }
        }
        return null;
    }

    //used annotation nonnull to prevent null pointer exception from taking place
    @NonNull
    public static <T> List<T> childrenOfType(@NonNull View root, @NonNull Class<T> type) {
        //concept of T to enable parametization
        final List<T> children = new ArrayList<>();
        if (type.isInstance(root)) {
            children.add(type.cast(root));
        }
        //used a loop to enable childeren count within the List and return the childeren(data)
        if (root instanceof ViewGroup) {
            final ViewGroup rootGroup = (ViewGroup) root;
            for (int i = 0; i < rootGroup.getChildCount(); i++) {
                final View child = rootGroup.getChildAt(i);
                children.addAll(childrenOfType(child, type));
            }
        }
        return children;
    }
}
