package de.blau.android.easyedit;

import java.util.List;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.PopupMenu;
import de.blau.android.App;
import de.blau.android.Main;
import de.blau.android.R;
import de.blau.android.layer.LayerType;
import de.blau.android.osm.Node;
import de.blau.android.osm.OsmElement;
import de.blau.android.osm.ViewBox;
import de.blau.android.tasks.NoteFragment;
import de.blau.android.util.GeoMath;
import de.blau.android.util.Snack;
import de.blau.android.util.ThemeUtils;

public class SimpleActionModeCallback extends EasyEditActionModeCallback implements android.view.MenuItem.OnMenuItemClickListener {
    private static final String DEBUG_TAG = "SimpleActionMode...";

    interface SimpleActionCallback {
        /**
         * Executes the actual code associated with a SimpleAction
         * 
         * @param main the current Main instance
         * @param manager the current EasyEditManager
         * @param x screen x coordinate
         * @param y screen y coordinate
         */
        void action(@NonNull final Main main, @NonNull final EasyEditManager manager, final float x, final float y);
    }

    public enum SimpleAction {
        /**
         * Add a node without merging with nearby elements, start the PropertyEditor with the Preset tab
         */
        NODE_TAGS(R.string.menu_add_node_tags, R.string.simple_add_node, (main, manager, x, y) -> {
            de.blau.android.Map map = main.getMap();
            ViewBox box = map.getViewBox();
            int width = map.getWidth();
            int height = map.getHeight();
            Node node = App.getLogic().performAddNode(main, GeoMath.xToLonE7(width, box, x), GeoMath.yToLatE7(height, width, box, y));
            main.startSupportActionMode(new NodeSelectionActionModeCallback(manager, node));
            main.performTagEdit(node, null, false, true);
        }) {
            @Override
            public boolean isEnabled() {
                return App.getLogic().getMode().enabledSimpleActions().contains(this);
            }
        },
        /**
         * Add an address node with with nearby elements, start the PropertyEditor with the Preset tab
         */
        ADDRESS_NODE(R.string.menu_add_node_address, R.string.simple_add_node, (main, manager, x, y) -> {
            App.getLogic().performAdd(main, x, y);
            Node node = App.getLogic().getSelectedNode();
            main.startSupportActionMode(new NodeSelectionActionModeCallback(manager, node));
            main.performTagEdit(node, null, true, false);
        }) {
            @Override
            public boolean isEnabled() {
                return App.getLogic().getMode().enabledSimpleActions().contains(this);
            }
        },
        /**
         * Add a way starting the normal path creation mode
         */
        WAY(R.string.menu_add_way, R.string.simple_add_way,
                (main, manager, x, y) -> main.startSupportActionMode(new PathCreationActionModeCallback(manager, x, y))) {
            @Override
            public boolean isEnabled() {
                return App.getLogic().getMode().enabledSimpleActions().contains(this);
            }
        },
        /**
         * Add a way starting the normal path creation mode
         */
        INTERPOLATION_WAY(R.string.menu_add_address_interpolation, R.string.simple_add_way,
                (main, manager, x, y) -> main.startSupportActionMode(new AddressInterpolationActionModeCallback(manager, x, y))) {
            @Override
            public boolean isEnabled() {
                return App.getLogic().getMode().enabledSimpleActions().contains(this);
            }
        },
        /**
         * Add a note
         */
        NOTE(R.string.menu_add_map_note, R.string.simple_add_note, (main, manager, x, y) -> {
            manager.finish();
            de.blau.android.layer.tasks.MapOverlay layer = main.getMap().getTaskLayer();
            if (layer == null) { // turn it on, this is supposed to be "simple"
                de.blau.android.layer.Util.addLayer(main, LayerType.TASKS);
                main.getMap().setUpLayers(main);
                layer = main.getMap().getTaskLayer();
                if (layer == null) {
                    Snack.toastTopError(main, R.string.toast_unable_to_create_task_layer);
                    return;
                }
                main.getMap().invalidate();
            }
            NoteFragment.showDialog(main, App.getLogic().makeNewNote(x, y));
        }) {
            @Override
            public boolean isEnabled() {
                return App.getLogic().getMode().enabledSimpleActions().contains(this);
            }
        },
        /**
         * Add a node, merging with nearby elements
         */
        NODE(R.string.menu_add_node, R.string.simple_add_node, (main, manager, x, y) -> {
            App.getLogic().performAdd(main, x, y);
            main.startSupportActionMode(new NodeSelectionActionModeCallback(manager, App.getLogic().getSelectedNode()));
        }) {
            @Override
            public boolean isEnabled() {
                return App.getLogic().getMode().enabledSimpleActions().contains(this);
            }
        },
        /**
         * Paste an object from the clipboard
         */
        PASTE(R.string.menu_paste_object, R.string.simple_paste, SimpleActionModeCallback::paste) {
            @Override
            public boolean isEnabled() {
                return !App.getLogic().clipboardIsEmpty();
            }
        },
        /**
         * Paste an object from the clipboard, without exiting the action mode
         */
        PASTEMULTIPLE(R.string.menu_paste_multiple, R.string.simple_paste_multiple, (main, manager, x, y) -> App.getLogic().pasteFromClipboard(main, x, y)) {
            @Override
            public boolean isEnabled() {
                return !App.getLogic().clipboardIsEmpty() && !App.getDelegator().clipboardContentWasCut();
            }
        };

        final int                  menuTextId;
        final int                  titleId;
        final SimpleActionCallback actionCallback;

        /**
         * Construct a SimpleAction
         * 
         * @param menuTextId resource for the menu text
         * @param titleId resource for the text displayed in the ActionMode
         * @param actionCallback callback with actual code for the action
         */
        SimpleAction(final int menuTextId, final int titleId, @NonNull final SimpleActionCallback actionCallback) {
            this.menuTextId = menuTextId;
            this.titleId = titleId;
            this.actionCallback = actionCallback;
        }

        /**
         * Get the resource id for the menu text
         * 
         * @return the resource id for the menu text
         */
        public int getMenuTextId() {
            return menuTextId;
        }

        /**
         * Get the resource id for the ActionMode text
         * 
         * @return the resource id for the ActionMode text
         */
        public int getTitleTextId() {
            return titleId;
        }

        /**
         * Execute the callback with the code for the action
         * 
         * @param main the current Main instance
         * @param manager the current EasyEditManager
         * @param x screen x coordinate
         * @param y screen y coordinate
         */
        public void execute(final Main main, final EasyEditManager manager, final float x, final float y) {
            actionCallback.action(main, manager, x, y);
        }

        /**
         * Check if this entry should be displayed
         * 
         * @return true if enabled
         */
        public boolean isEnabled() {
            return true;
        }
    }

    private final SimpleAction simpleAction;

    /**
     * Construct a callback for when a simple action has been selected
     * 
     * @param manager the EasyEditManager instance
     * @param simpleMode Enum indicating what to do
     */
    public SimpleActionModeCallback(@NonNull EasyEditManager manager, @NonNull SimpleAction simpleMode) {
        super(manager);
        this.simpleAction = simpleMode;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        helpTopic = R.string.help_simple_actions;
        super.onCreateActionMode(mode, menu);
        mode.setTitle(simpleAction.titleId);
        mode.setSubtitle(null);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu = replaceMenu(menu, mode, this);
        super.onPrepareActionMode(mode, menu);
        menu.clear();
        menuUtil.reset();
        menu.add(GROUP_BASE, MENUITEM_HELP, Menu.CATEGORY_SYSTEM | 10, R.string.menu_help).setIcon(ThemeUtils.getResIdFromAttribute(main, R.attr.menu_help));
        arrangeMenu(menu);
        return true;
    }

    @Override
    public boolean handleClick(float x, float y) {
        simpleAction.execute(main, manager, x, y);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        logic.setSelectedNode(null);
        logic.setSelectedWay(null);
        logic.setSelectedRelation(null);
        main.getMap().deselectObjects();
        super.onDestroyActionMode(mode);
    }

    @Override
    public boolean onMenuItemClick(MenuItem arg0) {
        return false;
    }

    /**
     * Get a menu suitable for display by clicking on a button
     * 
     * @param main the current instance of Main
     * @param anchor the anchor View for the popup
     * @return a PopupMenu instance
     */
    @NonNull
    public static PopupMenu getMenu(@NonNull Main main, @NonNull View anchor) {
        PopupMenu popup = new PopupMenu(main, anchor);
        for (SimpleAction simpleMode : SimpleAction.values()) {
            if (simpleMode.isEnabled()) {
                MenuItem item = popup.getMenu().add(simpleMode.getMenuTextId());
                item.setOnMenuItemClickListener(arg0 -> {
                    main.startSupportActionMode(new SimpleActionModeCallback(main.getEasyEditManager(), simpleMode));
                    return true;
                });
            }
        }
        return popup;
    }

    /**
     * Paste objects to a location
     * 
     * @param activity optional calling Activity
     * @param manager the current EasyEditManager
     * @param x screen x
     * @param y screen y
     */
    public static void paste(@Nullable final Activity activity, @NonNull final EasyEditManager manager, final float x, final float y) {
        List<OsmElement> elements = App.getLogic().pasteFromClipboard(activity, x, y);
        if (elements != null && !elements.isEmpty()) {
            if (elements.size() > 1) {
                manager.finish();
                App.getLogic().setSelection(elements);
                manager.editElements();
            } else {
                manager.editElement(elements.get(0));
            }
        } else {
            manager.finish();
        }
    }
}
