package com.example.har_app;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class Classifier {

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;
    private static final String MODEL_FILE = "file:///android_asset/frozen_HAR.pb";
    // Name of the input node of the LSTM
    private static final String INPUT_NODE = "LSTM_1_input";
    // Name of the output node of the LSTM
    private static final String[] OUTPUT_NODES = {"Dense_2/Softmax"};
    private static final String OUTPUT_NODE = "Dense_2/Softmax";
    // Size of the input data
    private static final long[] INPUT_SIZE = {1, 100, 12};
    // Six classes to classify
    private static final int OUTPUT_SIZE = 7;

    public Classifier(final Context context) {
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    // Feeds  the imported model with data from mobile sensors. Returns the activity that is being done.
    public float[] predictProbabilities(float[] data) {

        float[] result = new float[OUTPUT_SIZE];
        inferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE, result);

        return result;
    }


}
