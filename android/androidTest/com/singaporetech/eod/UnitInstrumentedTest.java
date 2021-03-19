package com.singaporetech.eod;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import android.app.Instrumentation;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.RunWith;

import androidx.test.uiautomator.*;

import com.boliao.eod.R;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.*;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
//import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class UnitInstrumentedTest {
    private static final String TAG = "UnitInstrumentedTest";
    protected static final int REPORT_ITEM_MAX_LENGTH = 100;

    // UiAutomator device
    protected UiDevice device;

    @Rule
    public TestLogger testLogger = new TestLogger();

    @Rule
    public ActivityTestRule<Splash> activityRule = new ActivityTestRule<>(Splash.class, false, true);

    @Before
    public void setupUiAutomator() {
        device = UiDevice.getInstance(getInstrumentation());
    }

    @Test
    public void onNewUsername_successStart() {
        final String name1 = "chektien";
        final String name2 = "tienchek";
        Log.i(TAG, "### On Splash View" +
                "\n- input " + name1 + " and " + name2 + " and click PLAY" +
                "\n- expected see a view of type GLSurfaceView20");

        // check whether the name_edtxt view actually exists
        onView(ViewMatchers.withId(R.id.name_edtxt)).check(matches(isDisplayed()));

        // check successful login
        onView(withId(R.id.name_edtxt))
                .perform(clearText(), typeText(name1), closeSoftKeyboard());
        onView(withText(equalToIgnoringCase("PLAY"))).perform(click());
        onView(withClassName(endsWith("GLSurfaceView20"))).check(matches(isDisplayed()));

        // check successful login
        pressBack();
        onView(withId(R.id.name_edtxt))
                .perform(clearText(), typeText(name2), closeSoftKeyboard());
        onView(withText(equalToIgnoringCase("PLAY"))).perform(click());
        onView(withClassName(endsWith("GLSurfaceView20"))).check(matches(isDisplayed()));
    }

    /**
     * UNINSTALL prev ver to clear shared prefs before running this test
     */
    @Test
    public void onExistingUsername_failedStart() {
        final String name1 = "chektien";
        final String name2 = "chek";
        final String errMsg = "Name already exists!";
        Log.i(TAG, "### On Splash View" +
                "\n- click PLAY with alternating inputs " + name1 + " amd " + name2 +
                "\n- expected see a view of type GLSurfaceView20" +
                "\n- then input same name again and click PLAY" +
                "\n- expected to see " + errMsg + "");

        // check base case
        onView(withId(R.id.name_edtxt))
                .perform(clearText(), typeText(name1), closeSoftKeyboard());
        onView(withText(equalToIgnoringCase("PLAY"))).perform(click());
        onView(withClassName(endsWith("GLSurfaceView20"))).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.name_edtxt))
                .perform(clearText(), typeText(name1), closeSoftKeyboard());
        onView(withText(equalToIgnoringCase("PLAY"))).perform(click());
        onView(withId(R.id.msg_txtview)).check(matches(withText(containsString(errMsg))));

        // now check if it is not simply storing the last username
        onView(withId(R.id.name_edtxt))
                .perform(clearText(), typeText(name2), closeSoftKeyboard());
        onView(withText(equalToIgnoringCase("PLAY"))).perform(click());
        onView(withClassName(endsWith("GLSurfaceView20"))).check(matches(isDisplayed()));

        pressBack();
        onView(withId(R.id.name_edtxt))
                .perform(clearText(), typeText(name1), closeSoftKeyboard());
        onView(withText(equalToIgnoringCase("PLAY"))).perform(click());
        onView(withId(R.id.msg_txtview)).check(matches(withText(containsString(errMsg))));
    }

    /**
     * Internal unit test for the isNumeric test.
     * - uncomment @Test if wanna use this
     */
//    @Test
    public void utest_is7Numeric() {
        assertEquals(true, is7Numeric("E1234567A"));
    }

    /**
     * Check whether a str is numeric
     * @param str
     * @return whether is numeric or not
     */
    private boolean is7Numeric(String str) {
        return str.matches("^.[0-9]{7}.$");
    }

    /**
     * Custom ViewMatcher to check if text is 4 digits
     */
    public Matcher<View> is7Numeric() {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            protected boolean matchesSafely(TextView item) {
                try {
                    return is7Numeric(item.getText().toString());
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is numeric");
            }
        };
    }

    /**
     * Perform action of waiting for some millis.
     */
    public static ViewAction waitMillis(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + millis + "ms";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                uiController.loopMainThreadUntilIdle();
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    /**
     * This will watch all tests in a rule and do post actions.
     * 1. Add test info to report str
     * 2. accumulate marks.
     */
    public class TestLogger extends TestWatcher {
        @Override
        protected void succeeded(org.junit.runner.Description description) {
            super.succeeded(description);

            Log.i(TAG, "- test PASSED.  \n");
        }

        @Override
        protected void failed(Throwable e, org.junit.runner.Description description) {
            super.failed(e, description);

            String errStr = e.getMessage();
            if (errStr.length() > REPORT_ITEM_MAX_LENGTH)
                errStr = errStr.split("\n")[0] + "... (err msg too long)";
            Log.i(TAG, "- FAILED Reason-> " + errStr + "  \n");
        }
    }
}

