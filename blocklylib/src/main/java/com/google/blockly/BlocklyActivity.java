/*
 *  Copyright  2015 Google Inc. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.blockly;

import android.content.res.AssetManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.blockly.model.BlocklySerializerException;
import com.google.blockly.model.Workspace;

import java.io.FileNotFoundException;

/**
 * Activity holding a full blockly workspace.
 */
public class BlocklyActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String TAG = "BlocklyActivity";

    public static final String WORKSPACE_FOLDER_PREFIX = "sample_";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private Workspace mWorkspace;
    private ToolboxFragment mToolboxFragment;
    private DrawerLayout mDrawerLayout;
    private TrashFragment mOscar;
    private WorkspaceFragment mWorkspaceFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int mCurrentPosition;

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (position == mCurrentPosition) {
            return;
        }

        mWorkspace = createWorkspace(position);
        mWorkspace.resetWorkspaceView();

        onSectionAttached(position + 1);    // Because indexing.
        mCurrentPosition = position;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_save) {
            Workspace workspace = mWorkspaceFragment.getWorkspace();
            try {
                workspace.serializeToXml(openFileOutput("workspace.xml", Context.MODE_PRIVATE));
            } catch (FileNotFoundException | BlocklySerializerException e) {
                e.printStackTrace();
            }
            return true;
        } else if (id == R.id.action_load) {
            Workspace workspace = mWorkspaceFragment.getWorkspace();
            try {
                workspace.loadFromXml(openFileInput("workspace.xml"));

                workspace.initBlockViews();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        } else if (id == R.id.action_airstrike) {
            mToolboxFragment.airstrike();
            return true;
        } else if (id == R.id.action_carpet_bomb) {
            mToolboxFragment.carpetBomb();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            default:
                break;
        }
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockly);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);

        mToolboxFragment =
                (ToolboxFragment) getSupportFragmentManager().findFragmentById(R.id.toolbox);

        // Set up the toolbox that lives inside the trash can.
        mOscar = (TrashFragment) getSupportFragmentManager().findFragmentById(R.id.trash);
        if (mOscar != null) {
            // Start hidden.
            getSupportFragmentManager().beginTransaction()
                    .hide(mOscar)
                    .commit();
        }
        mWorkspace = createWorkspace(1);
        mCurrentPosition = 0;
    }

    /**
     * Build the workspace for this activity.
     */
    protected Workspace createWorkspace(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        mWorkspaceFragment = (WorkspaceFragment) fragmentManager.findFragmentById(R.id.container);
        if (mWorkspaceFragment == null) {
            mWorkspaceFragment = new WorkspaceFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mWorkspaceFragment)
                    .commit();
        }

        AssetManager assetManager = getAssets();

        Workspace.Builder bob = new Workspace.Builder(this);
        bob.setBlocklyStyle(R.style.BlocklyTheme);
        bob.setAssetManager(assetManager);
        bob.addBlockDefinitionsFromAsset(
                WORKSPACE_FOLDER_PREFIX + (position + 1) + "/block_definitions.json");
        bob.setToolboxConfigurationAsset(WORKSPACE_FOLDER_PREFIX + (position + 1) + "/toolbox.xml");
        bob.setWorkspaceFragment(mWorkspaceFragment);
        bob.setTrashFragment(mOscar);
        bob.setToolboxFragment(mToolboxFragment, mDrawerLayout);
        bob.setFragmentManager(getSupportFragmentManager());
        return bob.build();
    }
}
