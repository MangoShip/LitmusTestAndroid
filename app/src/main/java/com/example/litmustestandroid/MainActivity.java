package com.example.litmustestandroid;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litmustestandroid.DialogFragments.*;
import com.example.litmustestandroid.Fragment.*;
import com.example.litmustestandroid.HelperClass.*;
import com.example.litmustestandroid.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static {
        System.loadLibrary("litmusTest-main-lib");
    }

    private ActivityMainBinding binding;

    private DrawerLayout drawer;

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;

    private TestViewObject currTestViewObject;
    private MultiTestViewObject currMultiTestViewObject;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog exploreDialog;
    private TextView exploreTestName;
    private Button exploreStartButton, exploreCloseButton, defaultParamButton, stressParamButton;
    private String currTestIterations;

    private AlertDialog tuningDialog;
    private TextView tuningTestName;
    private EditText[] tuningParameters = new EditText[7];
    private Button tuningStartButton, tuningCloseButton;
    private String tuningRandomSeed;
    private String[] tuningTestArgument = new String[4];
    private int tuningCurrConfig, tuningEndConfig, tuningTestWorkgroups, tuningMaxWorkgroups, tuningMinWorkgroupSize, tuningMaxWorkgroupSize;
    private ArrayList<TuningResultCase> currTuningResults = new ArrayList<TuningResultCase>();
    private HashMap<String, ArrayList<TuningResultCase>> tuningResultCases = new HashMap<>();

    private ArrayList<TestCase> multiSelectedTestCases = new ArrayList<TestCase>();
    private int multiCurrIteration;
    private RecyclerView currMultiTestRV;

    private static final String TAG = "MainActivity";

    private String currTestType = "";
    private TestCase currTestCase;
    private String GPUName = "";

    private Handler handler = new Handler();

    private String shaderType = "";

    private ArrayList<TestCase> testCases = new ArrayList<>();
    private LinkedHashMap<String, Boolean> multiTestCases = new LinkedHashMap<String, Boolean>();

    public ArrayList<String> totalShaderNames = new ArrayList<String>();
    public ArrayList<String> totalResultNames = new ArrayList<String>();
    public ArrayList<String> totalOutputNames = new ArrayList<String>();
    public ArrayList<String> totalTestParamNames = new ArrayList<String>();
    public ArrayList<String> totalParamPresetNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new Introduction()).commit();
        navigationView.setCheckedItem(R.id.nav_introduction);

        registerTestCases();
        initFileConfig();

        // Keeps screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_introduction:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new Introduction()).commit();
                break;
            case R.id.nav_multi_tests:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MultiTests()).commit();
                break;
            case R.id.nav_message_passing:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MessagePassing()).commit();
                break;
            case R.id.nav_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Store()).commit();
                break;
            case R.id.nav_read:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Read()).commit();
                break;
            case R.id.nav_load_buffer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LoadBuffer()).commit();
                break;
            case R.id.nav_store_buffer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new StoreBuffer()).commit();
                break;
            case R.id.nav_22_write:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Write22()).commit();
                break;
            case R.id.nav_corr:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoRR()).commit();
                break;
            case R.id.nav_coww:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoWW()).commit();
                break;
            case R.id.nav_cowr:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoWR()).commit();
                break;
            case R.id.nav_corw1:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoRW1()).commit();
                break;
            case R.id.nav_corw2:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoRW2()).commit();
                break;
            case R.id.nav_atomicity:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Atomicity()).commit();
                break;
            case R.id.nav_barrier_store_load:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BarrierStoreLoad()).commit();
                break;
            case R.id.nav_barrier_load_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BarrierLoadStore()).commit();
                break;
            case R.id.nav_barrier_store_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BarrierStoreStore()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    private String JsonDataFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("test_list.json");
            int sizeOfFile = inputStream.available();
            byte[] bufferData = new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            json = new String(bufferData, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void registerTestCases() {
        try {
            JSONObject testJsonObject = new JSONObject(JsonDataFromAsset());
            JSONArray testArray = testJsonObject.getJSONArray("tests");
            for (int i = 0; i < testArray.length(); i++) {
                JSONObject testData = testArray.getJSONObject(i);

                TestCase newTest = new TestCase();
                newTest.testName = testData.getString("name");
                newTest.testType = testData.getString("type");

                JSONArray shaderArray = testData.getJSONArray("shaders");
                String[] shaderNames = new String[shaderArray.length()];
                for(int j = 0; j < shaderArray.length(); j++) {
                    shaderNames[j] = shaderArray.getString(j);
                }
                newTest.setShaderNames(shaderNames, totalShaderNames);

                newTest.setResultName(testData.getString("result"), totalResultNames);

                JSONArray outputArray = testData.getJSONArray("outputs");
                String[] outputNames = new String[outputArray.length()];
                for(int j = 0; j < outputArray.length(); j++) {
                    outputNames[j] = outputArray.getString(j);
                }
                newTest.setOutputName(outputNames, totalOutputNames);

                newTest.setTestParamName(testData.getString("parameter"), totalTestParamNames);

                JSONArray paramPresetArray = testData.getJSONArray("parameter_presets");
                String[] paramPresetNames = new String[paramPresetArray.length()];
                for(int j = 0; j < paramPresetArray.length(); j++) {
                    paramPresetNames[j] = paramPresetArray.getString(j);
                }
                newTest.setParamPresetNames(paramPresetNames, totalParamPresetNames);

                testCases.add(newTest);
                multiTestCases.put(newTest.testName, false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public TestCase findTestCase(String testName) {
        for (int i = 0; i < testCases.size(); i++) {
            if(testCases.get(i).testName.equals(testName)) {
                return testCases.get(i);
            }
        }
        return null;
    }

    private void copyFile(int fromResId, String toFile) {
        InputStream is =  getResources().openRawResource(fromResId);
        byte[] buffer = new byte[4096];
        try
        {
            FileOutputStream fos = openFileOutput(toFile, Context.MODE_PRIVATE);

            int bytes_read;
            while ((bytes_read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytes_read);
            }

            fos.close();
            is.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void initFileConfig() {
        // Transfer shader files
        for(int i = 0; i < totalShaderNames.size(); i++) {
            int shaderId = this.getResources().getIdentifier(totalShaderNames.get(i), "raw", this.getPackageName());
            copyFile(shaderId, totalShaderNames.get(i) + ".spv");
            Log.d(TAG, "File: " + totalShaderNames.get(i) + ".spv copied to " + getFilesDir().toString());
        }

        // Transfer result files
        for(int i = 0; i < totalResultNames.size(); i++) {
            int resultId = this.getResources().getIdentifier(totalResultNames.get(i), "raw", this.getPackageName());
            copyFile(resultId, totalResultNames.get(i) + ".spv");
            Log.d(TAG, "File: " + totalResultNames.get(i) + ".spv copied to " + getFilesDir().toString());
        }

        // Transfer output files
        for(int i = 0; i < totalOutputNames.size(); i++) {
            int outputId = this.getResources().getIdentifier(totalOutputNames.get(i), "raw", this.getPackageName());
            copyFile(outputId, totalOutputNames.get(i) + ".txt");
            Log.d(TAG, "File: " + totalOutputNames.get(i) + ".txt copied to " + getFilesDir().toString());
        }

        // Transfer test param files
        for(int i = 0; i < totalTestParamNames.size(); i++) {
            int testParamId = this.getResources().getIdentifier(totalTestParamNames.get(i), "raw", this.getPackageName());
            copyFile(testParamId, totalTestParamNames.get(i) + ".txt");
            Log.d(TAG, "File: " + totalTestParamNames.get(i) + ".txt copied to " + getFilesDir().toString());
        }

        // Transfer param files
        for(int i = 0; i < totalParamPresetNames.size(); i++) {
            int paramPresetId = this.getResources().getIdentifier(totalParamPresetNames.get(i), "raw", this.getPackageName());
            copyFile(paramPresetId, totalParamPresetNames.get(i) + ".txt");
            Log.d(TAG, "File: " + totalParamPresetNames.get(i) + ".txt copied to " + getFilesDir().toString());
        }
    }

    // Automatically fill parameters with basic values
    public void loadParameters(EditText[] parameters, int paramValue){
        InputStream inputStream = getResources().openRawResource(paramValue);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        int index = 0;

        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] words = line.split("=");
                if((!words[0].equals("numMemLocations") && !words[0].equals("numOutputs")) && index < parameters.length) {
                    parameters[index].setText(words[1]);
                    index++;
                }
                line = bufferedReader.readLine();
            }
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Read and write current parameters value for testing
    public void writeParameters(String testName, EditText[] parameters, int paramValue) {
        InputStream inputStream = getResources().openRawResource(paramValue);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String fileName = "litmustest_" + testName + "_parameters.txt";
        try{
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            String line = bufferedReader.readLine();
            int index = 0;

            while (line != null) {
                String[] words = line.split("=");
                String outputNumber;
                String newLine = "\n";

                if(words[0].equals("numMemLocations") || words[0].equals("numOutputs")
                || words[0].equals("permuteFirst") || words[0].equals("permuteSecond")
                || words[0].equals("aliasedMemory") || words[0].equals("gpuDeviceId")) {
                    if(words[0].equals("gpuDeviceId")) {
                        newLine = "";
                    }
                    outputNumber = words[1];
                }
                else {
                    if(words[0].equals("testIterations")) {
                        currTestIterations = parameters[index].getText().toString();
                    }
                    outputNumber = parameters[index].getText().toString();
                    index++;
                }
                String outputLine = words[0] + "=" + outputNumber + newLine;
                fos.write(outputLine.getBytes());

                line = bufferedReader.readLine();
            }
            inputStream.close();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int randomGenerator(int min, int max, String tuningRandomSeed) {
        double generator;
        Random random;
        if(tuningRandomSeed.length() == 0) {
            random = new Random();
        }
        else {
            random = new Random(tuningRandomSeed.hashCode());
        }
        generator = random.nextDouble();
        return (int) Math.floor(generator * (max - min + 1) + min);
    }

    public int roundedPercentage(String tuningRandomSeed) {
        return (int) Math.floor(randomGenerator(0, 100, tuningRandomSeed) / 5) * 5;
    }

    public int getPercentage(String tuningRandomSeed, boolean smoothedParameters) {
        if (smoothedParameters) {
            return roundedPercentage(tuningRandomSeed);
        }
        else {
            return randomGenerator(0, 1, tuningRandomSeed) * 100;
        }
    }

    public void writeTuningParameters(String testName, int paramPresetValue) {
        boolean smoothedParameters = true;
        int workgroupLimiter = tuningMaxWorkgroups;
        int testingWorkgroups = randomGenerator(tuningTestWorkgroups, workgroupLimiter, tuningRandomSeed);
        int stressLineSize = (int) Math.pow(2, randomGenerator(2, 10, tuningRandomSeed));
        int stressTargetLines = randomGenerator(1, 16, tuningRandomSeed);
        int memStride = randomGenerator(1, 7, tuningRandomSeed);
        Map<String, String> parameterFormat = new TreeMap<String, String>();

        InputStream inputStream = getResources().openRawResource(paramPresetValue);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String fileName = "litmustest_" + testName + "_parameters.txt";

        // Get parameter format by reading preset file
        try {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] words = line.split("=");
                parameterFormat.put(words[0], words[1]);
                line = bufferedReader.readLine();
            }
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Now randomize certain parmeter values
        parameterFormat.put("testIterations", currTestIterations);

        // Temporary fixed number of workgroup
        //parameterFormat.put("testingWorkgroups", Integer.toString(128));
        //parameterFormat.put("maxWorkgroups", Integer.toString(128));
        
        parameterFormat.put("testingWorkgroups", Integer.toString(testingWorkgroups));
        parameterFormat.put("maxWorkgroups", Integer.toString(tuningMaxWorkgroups));

        parameterFormat.put("minWorkgroupSize", Integer.toString(tuningMinWorkgroupSize));
        parameterFormat.put("maxWorkgroupSize", Integer.toString(tuningMaxWorkgroupSize));

        parameterFormat.put("shufflePct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("barrierPct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("scratchMemorySize", Integer.toString(32 * stressLineSize * stressTargetLines));
        parameterFormat.put("memStride", Integer.toString(memStride));
        parameterFormat.put("memStressPct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("memStressIterations", Integer.toString(randomGenerator(0, 1024, tuningRandomSeed)));
        parameterFormat.put("preStressPct", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));
        parameterFormat.put("preStressIterations", Integer.toString(randomGenerator(0, 128, tuningRandomSeed)));
        parameterFormat.put("stressLineSize", Integer.toString(stressLineSize));
        parameterFormat.put("stressTargetLines", Integer.toString(stressTargetLines));
        parameterFormat.put("stressAssignmentStrategy", Integer.toString(getPercentage(tuningRandomSeed, smoothedParameters)));

        boolean memStressStoreFirst = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        boolean memStressStoreSecond = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        int memStressPattern;
        if (memStressStoreFirst) {
            if (memStressStoreSecond) {
                memStressPattern = 0;
            }
            else {
                memStressPattern = 1;
            }
        }
        else {
            if (memStressStoreSecond) {
                memStressPattern = 2;
            }
            else {
                memStressPattern = 3;
            }
        }
        parameterFormat.put("memStressPattern", Integer.toString(memStressPattern));

        boolean preStressStoreFirst = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        boolean preStressStoreSecond = Math.floor(Math.random() * 100) < getPercentage(tuningRandomSeed, smoothedParameters);
        int preStressPattern;
        if (preStressStoreFirst) {
            if (preStressStoreSecond) {
                preStressPattern = 0;
            }
            else {
                preStressPattern = 1;
            }
        }
        else {
            if (preStressStoreSecond) {
                preStressPattern = 2;
            }
            else {
                preStressPattern = 3;
            }
        }
        parameterFormat.put("preStressPattern", Integer.toString(preStressPattern));

        // Now insert the parameter values to text file that will be used during test

        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            for (String key: parameterFormat.keySet()) {
                fos.write((key + "=" + parameterFormat.get(key) + "\n").getBytes());
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void handleButtons(boolean testBegin, Button[] buttons, ResultButton[] resultButtons) {
        // button[0] = currently running test's button
        // button[1] = currently not running test's button
        // resultButton[0] = currently running test's result button
        // resultButton[1] = currently not running test's result button
        if(testBegin) { // Test starting
            for(int i = 0; i < buttons.length; i++) {
                buttons[i].setEnabled(false);

                if(i == 0) {
                    buttons[i].setBackgroundColor(getResources().getColor(R.color.cyan));
                }
                else {
                    buttons[i].setBackgroundColor(getResources().getColor(R.color.lightgray));
                }
                resultButtons[i].button.setEnabled(false);
                resultButtons[i].button.setBackgroundColor(getResources().getColor(R.color.lightgray));
            }
        }
        else { // Test ended
            buttons[0].setEnabled(true);
            buttons[0].setBackgroundColor(getResources().getColor(R.color.lightblue));
            buttons[1].setEnabled(true);
            buttons[1].setBackgroundColor(getResources().getColor(R.color.lightblue));

            resultButtons[0].button.setEnabled(true);
            resultButtons[0].button.setBackgroundColor(getResources().getColor(R.color.red));
            resultButtons[0].isNew = false;

            if(!resultButtons[1].isNew) {
                resultButtons[1].button.setEnabled(true);
                resultButtons[1].button.setBackgroundColor(getResources().getColor(R.color.red));
            }
        }
    }

    public void initializeShaderMenu(String testName, View exploreMenuView) {
        autoCompleteTextView = exploreMenuView.findViewById(R.id.shaderExploreAutoCompleteText);

        TestCase currTestCase = findTestCase(testName);
        String[] shortShaderNames = new String[currTestCase.shaderNames.length];
        for (int i = 0; i < currTestCase.shaderNames.length; i++) {
            shortShaderNames[i] = currTestCase.shaderNames[i].substring(testName.length() + 12);
        }

        if(currTestCase != null) {
            adapterItems = new ArrayAdapter<String>(this, R.layout.shader_explore_list_item, shortShaderNames);
            autoCompleteTextView.setAdapter(adapterItems);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String item = adapterView.getItemAtPosition(i).toString();
                    shaderType = "litmustest_" + testName + "_" + item;
                }
            });
        }
        else {
            Log.e(TAG, "initializeShaderMenu(), currTestCase is NULL");
        }

    }

    public void openExploreMenu(String testName, TestViewObject testViewObject) {
        Log.i("TEST", testName + " PRESSED, OPENING Explore MENU");

        currTestViewObject = testViewObject;
        currTestType = "Explorer";

        dialogBuilder = new AlertDialog.Builder(this);
        final View exploreMenuView = getLayoutInflater().inflate(R.layout.main_test_explore, null);

        exploreTestName = (TextView) exploreMenuView.findViewById(R.id.testExploreTestName);
        exploreTestName.setText(testName);

        EditText[] exploreParameters = new EditText[18];
        exploreParameters[0] = (EditText) exploreMenuView.findViewById(R.id.testExploreTestIteration); // testIteration
        exploreParameters[1] = (EditText) exploreMenuView.findViewById(R.id.testExploreTestingWorkgroups); // testingWorkgroups
        exploreParameters[2] = (EditText) exploreMenuView.findViewById(R.id.testExploreMaxWorkgroups); // maxWorkgroups
        exploreParameters[3] = (EditText) exploreMenuView.findViewById(R.id.testExploreMinWorkgroupSize); // minWorkgroupSize
        exploreParameters[4] = (EditText) exploreMenuView.findViewById(R.id.testExploreMaxWorkgroupSize); // maxWorkgroupSize
        exploreParameters[5] = (EditText) exploreMenuView.findViewById(R.id.testExploreShufflePct); // shufflePct
        exploreParameters[6] = (EditText) exploreMenuView.findViewById(R.id.testExploreBarrierPct); // barrierPct
        exploreParameters[7] = (EditText) exploreMenuView.findViewById(R.id.testExploreScratchMemorySize); // scratchMemorySize
        exploreParameters[8] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStride); // memStride
        exploreParameters[9] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressPct); // memStressPct
        exploreParameters[10] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemoryStressIterations); // memStressIterations
        exploreParameters[11] = (EditText) exploreMenuView.findViewById(R.id.testExploreMemOryStressPattern); // memStressPattern
        exploreParameters[12] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressPct); // preStressPct
        exploreParameters[13] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressIterations); // preStressIterations
        exploreParameters[14] = (EditText) exploreMenuView.findViewById(R.id.testExplorePreStressPattern); // preStressPattern
        exploreParameters[15] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressLineSize); // stressLineSize
        exploreParameters[16] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressTargetLines); // stressTargetLines
        exploreParameters[17] = (EditText) exploreMenuView.findViewById(R.id.testExploreStressAssignmentStrategy); // stressAssignmentStrategy

        currTestCase = findTestCase(testName);
        int basic_parameters = this.getResources().getIdentifier(currTestCase.paramPresetNames[0], "raw", this.getPackageName());
        int stress_parameters = this.getResources().getIdentifier(currTestCase.paramPresetNames[1], "raw", this.getPackageName());

        loadParameters(exploreParameters, basic_parameters);

        exploreStartButton = (Button) exploreMenuView.findViewById(R.id.testExploreStartButton);
        exploreCloseButton = (Button) exploreMenuView.findViewById(R.id.testExploreCloseButton);
        defaultParamButton = (Button) exploreMenuView.findViewById(R.id.testExploreDefaultParamButton);
        stressParamButton = (Button) exploreMenuView.findViewById(R.id.testExploreStressParamButton);

        dialogBuilder.setView(exploreMenuView);
        exploreDialog = dialogBuilder.create();
        exploreDialog.show();

        // Reset shader type
        shaderType = currTestCase.shaderNames[0];

        // Initialize shader drop down explore menu
        initializeShaderMenu(testName, exploreMenuView);

        // Load default parameter
        defaultParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(exploreParameters, basic_parameters);
            }
        });

        // Load stress parameter
        stressParamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stressParamButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                defaultParamButton.setBackgroundColor(getResources().getColor(R.color.lightgray));
                loadParameters(exploreParameters, stress_parameters);
            }
        });

        // Start test
        exploreStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("EXPLORER TEST", testName + " STARTING");

                writeParameters(testName, exploreParameters, basic_parameters);
                exploreDialog.dismiss();

                // Disable buttons and change their color
                handleButtons(true, testViewObject.buttons, testViewObject.resultButtons);

                // Make progress layout visible
                testViewObject.explorerProgressLayout.setVisibility(View.VISIBLE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] testArgument = new String[4];
                        testArgument[0] = "litmustest_" + testName; // Test Name

                        // Shader Name
                        testArgument[1] = shaderType; // Current selected shader
                        testArgument[2] = currTestCase.resultName; // Result Shader
                        testArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

                        TestThread testThread = new TestThread(MainActivity.this, testArgument, false);
                        testThread.start();
                    }
                }, 500);
            }
        });

        // Close menu
        exploreCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", testName + " MENU CLOSING");
                exploreDialog.dismiss();
            }
        });

    }

    public void displayTestResult(String testName) {
        Log.i("RESULT", testName + " PRESSED");

        TestResultDialogFragment dialog = new TestResultDialogFragment();

        try
        {
            FileInputStream fis = openFileInput("litmustest_" + testName + "_output_explorer.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            dialog.setText(sb);
            Log.d(TAG, sb.toString());

            fis.close();
            isr.close();
            br.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        dialog.show(getSupportFragmentManager(), "TestResultDialog");
    }

    public String convertFileToString(String fileName) {
        String result = "";

        try {
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            result = sb.toString();

            fis.close();
            isr.close();
            br.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public void openTuningMenu(String testName, TestViewObject testViewObject) {
        Log.i("TUNING TEST", testName + " PRESSED, OPENING TUNING MENU");

        currTestViewObject = testViewObject;
        currTestType = "Tuning";

        dialogBuilder = new AlertDialog.Builder(this);
        final View tuningMenuView = getLayoutInflater().inflate(R.layout.main_test_tuning, null);

        tuningTestName = (TextView) tuningMenuView.findViewById(R.id.testTuningTestName);
        tuningTestName.setText(testName);

        tuningParameters[0] = (EditText) tuningMenuView.findViewById(R.id.testTuningConfigNum); // testConfigNum
        tuningParameters[1] = (EditText) tuningMenuView.findViewById(R.id.testTuningTestIteration); // testIteration
        tuningParameters[2] = (EditText) tuningMenuView.findViewById(R.id.testTuningRandomSeed); // randomSeed
        tuningParameters[3] = (EditText) tuningMenuView.findViewById(R.id.testTuningTestingWorkgroups); // testingWorkgroups
        tuningParameters[4] = (EditText) tuningMenuView.findViewById(R.id.testTuningMaxWorkgroups); // maxWorkgroups
        tuningParameters[5] = (EditText) tuningMenuView.findViewById(R.id.testTuningMinWorkgroupSize); // minWorkgroupSize
        tuningParameters[6] = (EditText) tuningMenuView.findViewById(R.id.testTuningMaxWorkgroupSize); // maxWorkgroupSize

        tuningStartButton = (Button) tuningMenuView.findViewById(R.id.testTuningStartButton);
        tuningCloseButton = (Button) tuningMenuView.findViewById(R.id.testTuningCloseButton);

        dialogBuilder.setView(tuningMenuView);
        tuningDialog = dialogBuilder.create();
        tuningDialog.show();

        currTestCase = findTestCase(testName);
        shaderType = currTestCase.shaderNames[0];

        // Start tuning test
        tuningStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TUNING TEST", testName + " STARTING");

                int tuningConfigNum = Integer.parseInt(tuningParameters[0].getText().toString());
                currTuningResults = new ArrayList<TuningResultCase>();
                tuningRandomSeed = tuningParameters[2].getText().toString();
                tuningTestWorkgroups = Integer.parseInt(tuningParameters[3].getText().toString());
                tuningMaxWorkgroups = Integer.parseInt(tuningParameters[4].getText().toString());
                tuningMinWorkgroupSize =  Integer.parseInt(tuningParameters[5].getText().toString());
                tuningMaxWorkgroupSize =  Integer.parseInt(tuningParameters[6].getText().toString());

                tuningDialog.dismiss();

                currTestIterations = tuningParameters[1].getText().toString();

                // Disable buttons and change their color
                handleButtons(true, testViewObject.buttons, testViewObject.resultButtons);

                // Make progress layout visible
                testViewObject.tuningProgressLayout.setVisibility(View.VISIBLE);

                tuningTestArgument[0] = "litmustest_" + testName; // Test Name

                // Shader Name
                tuningTestArgument[1] = shaderType; // Current selected shader
                tuningTestArgument[2] = currTestCase.resultName; // Result Shader
                tuningTestArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

                tuningCurrConfig = 0;
                tuningEndConfig = tuningConfigNum;

                tuningTestLoop(testName);
            }
        });

        // Close menu
        tuningCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", testName + " MENU CLOSING");
                tuningDialog.dismiss();
            }
        });
    }

    public void tuningTestLoop(String testName) {

        currTestViewObject.tuningCurrentConfigNumber.setText(tuningCurrConfig+1 + "/" + tuningEndConfig);

        writeTuningParameters(testName, this.getResources().getIdentifier(currTestCase.paramPresetNames[1], "raw", this.getPackageName()));

        // Run test in different thread
        TestThread testThread = new TestThread(MainActivity.this, tuningTestArgument, true);
        testThread.start();
    }

    public void tuningTestResult(String testName) {
        Log.i("TUNING RESULT", testName + " PRESSED");

        ArrayList<TuningResultCase> currTestList = tuningResultCases.get(testName);

        if(currTestList == null) {
            Log.e(TAG, testName + " cannot find result cases!");
        }

        TuningResultDialogFragment dialog = new TuningResultDialogFragment(testName, currTestList, MainActivity.this);
        dialog.show(getSupportFragmentManager(), "TuningResultDialog");
    }

    public void multiTestCheckBoxesListener(View view) {
        CheckBox currCheckBox = (CheckBox)view;
        String testName = view.getTag().toString();
        if(currCheckBox.isChecked()) { // Clicked
            multiTestCases.put(testName, true);
        }
        else { // Un-clicked
            multiTestCases.put(testName, false);
        }
     }

    public void multiExplorerTestBegin(EditText[] parameters, MultiTestViewObject multiTestViewObject, RecyclerView multiTestRV) {
        currTestType = "MultiExplorer";
        currMultiTestViewObject = multiTestViewObject;
        currMultiTestRV = multiTestRV;
        multiSelectedTestCases = new ArrayList<TestCase>();

        // Check if at least one test selected
        for (LinkedHashMap.Entry<String, Boolean> entry : multiTestCases.entrySet()) {
            if(entry.getValue() == true) {
                multiSelectedTestCases.add(findTestCase(entry.getKey()));
            }
        }
        if(multiSelectedTestCases.size() == 0) { // No test selected
            Toast.makeText(MainActivity.this, "No test selected!", Toast.LENGTH_LONG).show();
            return;
        }

        // Write parameters
        for (int i = 0; i < multiSelectedTestCases.size(); i++) {
            TestCase testCase = multiSelectedTestCases.get(i);
            int basic_parameters = this.getResources().getIdentifier(testCase.paramPresetNames[0], "raw", this.getPackageName());
            writeParameters(testCase.testName, parameters, basic_parameters);
        }

        // Disable start button
        currMultiTestViewObject.startButton.setEnabled(false);
        currMultiTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.cyan));

        // Set progress layout visible
        currMultiTestViewObject.progressLayout.setVisibility(View.VISIBLE);

        // Set config layout remain invisible
        currMultiTestViewObject.configLayout.setVisibility(View.GONE);

        // Set result layout invisible
        currMultiTestViewObject.explorerResultLayout.setVisibility(View.GONE);

        multiCurrIteration = 0;

        // Start multi explorer test loop
        multiExplorerTestLoop();
    }

    public void multiExplorerTestLoop() {

        currTestCase = multiSelectedTestCases.get(multiCurrIteration);
        String[] testArgument = new String[4];

        testArgument[0] = "litmustest_" + currTestCase.testName; // Test Name

        // Shader Name
        testArgument[1] = currTestCase.shaderNames[0]; // Current selected shader
        testArgument[2] = currTestCase.resultName; // Result Shader
        testArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

        // Update test name
        currMultiTestViewObject.currentTestName.setText(currTestCase.testName);

        // Run test in different thread
        TestThread testThread = new TestThread(MainActivity.this, testArgument, false);
        testThread.start();
    }

    public void multiTuningTestBegin(EditText[] parameters, MultiTestViewObject multiTestViewObject, RecyclerView multiTestRV) {
        currTestType = "MultiTuning";
        currMultiTestViewObject = multiTestViewObject;
        currMultiTestRV = multiTestRV;
        multiSelectedTestCases = new ArrayList<TestCase>();

        // Check if at least one test selected
        for (LinkedHashMap.Entry<String, Boolean> entry : multiTestCases.entrySet()) {
            if(entry.getValue() == true) {
                multiSelectedTestCases.add(findTestCase(entry.getKey()));
            }
        }
        if(multiSelectedTestCases.size() == 0) { // No test selected
            Toast.makeText(MainActivity.this, "No test selected!", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable start button
        currMultiTestViewObject.startButton.setEnabled(false);
        currMultiTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.cyan));

        // Set progress layout visible
        currMultiTestViewObject.progressLayout.setVisibility(View.VISIBLE);

        // Set config layout visible
        currMultiTestViewObject.configLayout.setVisibility(View.VISIBLE);

        // Set result layout invisible
        currMultiTestViewObject.tuningResultLayout.setVisibility(View.GONE);

        int tuningConfigNum = Integer.parseInt(parameters[0].getText().toString());
        currTuningResults = new ArrayList<TuningResultCase>();
        currTestIterations = parameters[1].getText().toString();
        tuningRandomSeed = parameters[2].getText().toString();
        tuningTestWorkgroups = Integer.parseInt(parameters[3].getText().toString());
        tuningMaxWorkgroups = Integer.parseInt(parameters[4].getText().toString());
        tuningMinWorkgroupSize = Integer.parseInt(parameters[5].getText().toString());
        tuningMaxWorkgroupSize = Integer.parseInt(parameters[6].getText().toString());

        tuningCurrConfig = 0;
        tuningEndConfig = tuningConfigNum;

        multiCurrIteration = 0;

        // Start multi tuning test loop
        multiTuningTestLoop();
    }

    public void multiTuningTestLoop() {
        currTestCase = multiSelectedTestCases.get(multiCurrIteration);
        String[] testArgument = new String[4];

        testArgument[0] = "litmustest_" + currTestCase.testName; // Test Name

        // Shader Name
        testArgument[1] = currTestCase.shaderNames[0]; // Current selected shader
        testArgument[2] = currTestCase.resultName; // Result Shader
        testArgument[3] = currTestCase.testParamName; // Txt file that stores parameter

        // Update test name
        currMultiTestViewObject.currentTestName.setText(currTestCase.testName);

        // Update current config number
        currMultiTestViewObject.currentConfigNumber.setText(tuningCurrConfig+1 + "/" + tuningEndConfig);

        // Write tuning parameter
        writeTuningParameters(currTestCase.testName, this.getResources().getIdentifier(currTestCase.paramPresetNames[1], "raw", this.getPackageName()));

        // Run test in different thread
        TestThread testThread = new TestThread(MainActivity.this, testArgument, true);
        testThread.start();
    }

    public String getFileDir() {
        return getFilesDir().toString();
    }

    public void iterationProgress(String iterationNum) {
        //Log.i(TAG, "IterationProgress: " + iterationNum + "/" + currTestIterations);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(currTestType.equals("Explorer")) {
                    currTestViewObject.explorerCurrentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals("Tuning")) {
                    currTestViewObject.tuningCurrentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else if (currTestType.equals("MultiExplorer")) {
                    currMultiTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
                else { // Multi Tuning test
                    currMultiTestViewObject.currentIterationNumber.setText(iterationNum + "/" + currTestIterations);
                }
            }
        });
    }

    public void setGPUName(String gpuName) {
        GPUName = gpuName;
        Log.i(TAG, gpuName);
    }

    public void multiTestSendResult(String testMode) {
        Log.i(TAG, "Sending result via email");

        String recipient[] = {"mingun0108@gmail.com"};
        String subject = "LitmusTestAndroid";
        String message = "GPU: " + GPUName;
        String fileName = "";

        if(testMode.equals("Explorer")) {
            subject += " MultiTest Explorer Result";
            fileName = "litmustest_multitest_explorer_result.txt";
        }
        else if (testMode.equals("Tuning")) {
            subject += " MultiTest Tuning Result";
            fileName = "litmustest_multitest_tuning_result.txt";
        }
        else { // Shouldn't be here
            Log.e(TAG, "multiTestSendResult invalid currTestType!: " + currTestType);
        }

        File fileLocation = new File(getFileDir(), fileName);
        Uri path = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", fileLocation);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipient);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);

        emailIntent.setType("plain/text");

        Intent chooser = Intent.createChooser(emailIntent, "Share File");

        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivity(chooser);
        }
        catch(android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
        }
    }

    public void testComplete() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(currTestType.equals("Explorer")) {
                    // Enable buttons and change their color
                    handleButtons(false, currTestViewObject.buttons, currTestViewObject.resultButtons);

                    currTestViewObject.explorerProgressLayout.setVisibility(View.GONE);

                    Toast.makeText(MainActivity.this, "Test " + currTestViewObject.testName + " finished!", Toast.LENGTH_LONG).show();
                }
                else if (currTestType.equals("Tuning")) {
                    // Save param value
                    String currParamValue = convertFileToString(currTestCase.testParamName + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(currTestCase.outputNames[1] + ".txt");

                    // Go through result and get number of weak behaviors
                    String startIndexIndicator = "weak: ";
                    String endIndexIndicator = "\nTotal elapsed time";
                    String numWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                    // Transfer over the tuning result case
                    TuningResultCase currTuningResult = new TuningResultCase(currParamValue, currResultValue, Integer.parseInt(numWeakBehaviors));

                    currTuningResults.add(currTuningResult);

                    if(tuningCurrConfig == tuningEndConfig - 1) {
                        tuningResultCases.put(currTestViewObject.testName, currTuningResults);

                        // Enable buttons and change their color
                        handleButtons(false, currTestViewObject.buttons, currTestViewObject.resultButtons);

                        currTestViewObject.tuningProgressLayout.setVisibility(View.GONE);

                        Toast.makeText(MainActivity.this, "Tuning Test " + currTestViewObject.testName + " finished!", Toast.LENGTH_LONG).show();
                    }
                    else {
                        tuningCurrConfig++;
                        tuningTestLoop(currTestViewObject.testName);
                    }
                }
                else if (currTestType.equals("MultiExplorer")){ // Multi Explorer test
                    // Append to result file
                    String outputFileName = "litmustest_multitest_explorer_result.txt";
                    try
                    {
                        FileOutputStream fos;
                        if(multiCurrIteration == 0) {
                            fos = openFileOutput(outputFileName, Context.MODE_PRIVATE);
                        }
                        else {
                            fos = openFileOutput(outputFileName, Context.MODE_APPEND);
                        }

                        FileInputStream fis = openFileInput("litmustest_" + multiSelectedTestCases.get(multiCurrIteration).testName + "_output_explorer.txt");
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader br = new BufferedReader(isr);

                        String line = br.readLine();
                        while (line != null) {
                            line += "\n";
                            fos.write(line.getBytes());
                            line = br.readLine();
                        }

                        String eof = "----------\n";
                        fos.write(eof.getBytes());

                        fis.close();
                        isr.close();
                        br.close();

                        fos.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    if(multiCurrIteration == multiSelectedTestCases.size()-1) { // All test ended, update result

                        Toast.makeText(MainActivity.this, "All tests have been completed!", Toast.LENGTH_LONG).show();

                        // Enable start button
                        currMultiTestViewObject.startButton.setEnabled(true);
                        currMultiTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.lightblue));

                        // Set progress layout invisible
                        currMultiTestViewObject.progressLayout.setVisibility(View.GONE);

                        // Set result layout visible
                        currMultiTestViewObject.explorerResultLayout.setVisibility(View.VISIBLE);

                        // Indicate that there is result to be displayed
                        currMultiTestViewObject.newExplorer = false;

                        // Get string array of test names
                        String[] testNames = new String[multiSelectedTestCases.size()];
                        for(int i = 0; i < multiSelectedTestCases.size(); i++) {
                            testNames[i] = multiSelectedTestCases.get(i).testName;
                        }

                        // Update result
                        MultiTestResultAdapter multiTestResultAdapter = new MultiTestResultAdapter(testNames, MainActivity.this, "Explorer");
                        currMultiTestRV.setAdapter(multiTestResultAdapter);
                        currMultiTestRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        currMultiTestRV.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
                    }
                    else {
                        multiCurrIteration++;
                        multiExplorerTestLoop();
                    }
                }
                else { // Multi Tuning Test
                    // Append to result file
                    String outputFileName = "litmustest_multitest_tuning_result.txt";
                    try
                    {
                        FileOutputStream fos;
                        if(multiCurrIteration == 0 && tuningCurrConfig == 0) {
                            fos = openFileOutput(outputFileName, Context.MODE_PRIVATE);
                        }
                        else {
                            fos = openFileOutput(outputFileName, Context.MODE_APPEND);
                        }

                        String header = currTestCase.testName + " Test " + (tuningCurrConfig + 1) + " Parameter: \n";
                        fos.write(header.getBytes());

                        FileInputStream fis = openFileInput(currTestCase.testParamName + ".txt");
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader br = new BufferedReader(isr);

                        String line = br.readLine();
                        while (line != null) {
                            line += "\n";
                            fos.write(line.getBytes());
                            line = br.readLine();
                        }
                        fos.write("\n".getBytes());
                        header = currTestCase.testName + " Test " + (tuningCurrConfig + 1) + " ";
                        fos.write(header.getBytes());

                        fis.close();
                        isr.close();
                        br.close();

                        fis = openFileInput(currTestCase.outputNames[1] + ".txt");
                        isr = new InputStreamReader(fis);
                        br = new BufferedReader(isr);

                        line = br.readLine();
                        while (line != null) {
                            line += "\n";
                            fos.write(line.getBytes());
                            line = br.readLine();
                        }

                        String eof = "----------\n";
                        fos.write(eof.getBytes());

                        fis.close();
                        isr.close();
                        br.close();
                        fos.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    // Save param value
                    String currParamValue = convertFileToString(currTestCase.testParamName + ".txt");

                    // Save result value
                    String currResultValue = convertFileToString(currTestCase.outputNames[1] + ".txt");

                    // Go through result and get number of weak behaviors
                    String startIndexIndicator = "weak: ";
                    String endIndexIndicator = "\nTotal elapsed time";
                    String numWeakBehaviors = currResultValue.substring(currResultValue.indexOf(startIndexIndicator) + startIndexIndicator.length(), currResultValue.indexOf(endIndexIndicator));

                    // Transfer over the tuning result case
                    TuningResultCase currTuningResult = new TuningResultCase(currParamValue, currResultValue, Integer.parseInt(numWeakBehaviors));

                    currTuningResults.add(currTuningResult);

                    if(tuningCurrConfig == tuningEndConfig - 1) { // One tuning test completed
                        tuningResultCases.put(currTestCase.testName, currTuningResults);
                        currTuningResults = new ArrayList<TuningResultCase>();

                        // Reset tuning config
                        tuningCurrConfig = 0;
                        multiCurrIteration++;

                        if(multiCurrIteration == multiSelectedTestCases.size()) { // All tuning tests completed

                            Toast.makeText(MainActivity.this, "All tests have been completed!", Toast.LENGTH_LONG).show();

                            // Enable start button
                            currMultiTestViewObject.startButton.setEnabled(true);
                            currMultiTestViewObject.startButton.setBackgroundColor(getResources().getColor(R.color.lightblue));

                            // Set progress layout invisible
                            currMultiTestViewObject.progressLayout.setVisibility(View.GONE);

                            // Set result layout visible
                            currMultiTestViewObject.tuningResultLayout.setVisibility(View.VISIBLE);

                            // Indicate that there is result to be displayed
                            currMultiTestViewObject.newTuning = false;

                            // Get string array of test names
                            String[] testNames = new String[multiSelectedTestCases.size()];
                            for(int i = 0; i < multiSelectedTestCases.size(); i++) {
                                testNames[i] = multiSelectedTestCases.get(i).testName;
                            }

                            // Update Result
                            MultiTestResultAdapter multiTestResultAdapter = new MultiTestResultAdapter(testNames, MainActivity.this, "Tuning");
                            currMultiTestRV.setAdapter(multiTestResultAdapter);
                            currMultiTestRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            currMultiTestRV.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
                        }
                        else {
                            multiTuningTestLoop();
                        }
                    }
                    else {
                        tuningCurrConfig++;
                        multiTuningTestLoop();
                    }
                }
            }
        });
    }
}
