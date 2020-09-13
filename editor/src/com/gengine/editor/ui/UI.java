package com.gengine.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gengine.editor.ui.dialog.material.MaterialConfigDialog;
import com.gengine.editor.ui.hierarchy.LibraryTree;
import com.gengine.editor.ui.inspector.EditorInspectorView;
import com.gengine.editor.ui.tools.EditorBrushSettingsView;
import com.gengine.editor.ui.tools.TexturePaintView;
import com.gengine.editor.ui.hierarchy.CellHierarchyTree;
import com.gengine.editor.ui.widgets.SceneRenderWidget;
import com.gengine.editor.ui.widgets.SplitPane;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.file.FileChooser;


public class UI extends Stage {

    private final VisTable rootTable;
    private final EditorBrushSettingsView editorBrushSettings;
    private final MaterialConfigDialog editorMaterials;

    private final VisTable hierarchy; //EditorCellHierarchyTree editorNodeTree;

    private final VisLabel fpsLabel;
    private final VisLabel drawCalls;
    private final VisLabel shaders;
    private final VisLabel textures;
    private final Cell toolSettingColumn;
    private final CellHierarchyTree cellHierarchy;
    private final LibraryTree libraryHierarchy;

    public SceneRenderWidget getSceneWidget() {
        return sceneWidget;
    }

    private TexturePaintView texturePaintView;

    private final SceneRenderWidget sceneWidget;
    private final EditorToolbar toolbar;
    private final EditorMenuBar menuBar;

    private FileChooser fileChooser;

    public TexturePaintView getTexturePaintView() {
        return texturePaintView;
    }

    public UI() {
        super(new ScreenViewport());
        VisUI.load();

        texturePaintView = new TexturePaintView();

        rootTable = new VisTable();
        addActor(rootTable);
        rootTable.setFillParent(true);


        sceneWidget = new SceneRenderWidget();
        //sceneWidget.setSize(640, 480);
        sceneWidget.setFillParent(true);

        toolbar = new EditorToolbar();
        menuBar = new EditorMenuBar();
        editorMaterials = new MaterialConfigDialog();


        fileChooser = new FileChooser(FileChooser.Mode.OPEN);

        rootTable.add(menuBar.getTable()).fillX().expandX().top().row();
        rootTable.add(toolbar.getRoot()).fillX().expandX().row();

        //rootTable.add(toolSettingBar).fillX().growX().row();
        toolSettingColumn = toolbar.getRight().add();

        hierarchy = new VisTable();


        VisTable hierarchT = new VisTable();
        hierarchy.add(cellHierarchy = new CellHierarchyTree()).expand().fill().top().uniform().row();
        hierarchT.pad(5);
        hierarchy.add(libraryHierarchy = new LibraryTree()).expand().fill().bottom().uniform().row();
        hierarchT.pack();

        hierarchy.add(hierarchT).uniform().fill().expand().row();

        editorBrushSettings = new EditorBrushSettingsView();

        VisTable debugInfo = new VisTable();
        debugInfo.add(fpsLabel = new VisLabel("FPS")).pad(5);
        debugInfo.add(drawCalls = new VisLabel("Draws")).pad(5);
        debugInfo.add(shaders = new VisLabel("Shaders")).pad(5);
        debugInfo.add(textures = new VisLabel("Textures")).pad(5);
        sceneWidget.add(debugInfo);

        SplitPane splitPane2 = new SplitPane(sceneWidget, new EditorInspectorView(), false);
        splitPane2.setMinSplitAmount(0.8f);
        splitPane2.setSplitAmount(0.8f);

        SplitPane splitPane = new SplitPane(hierarchy, splitPane2, false);

        splitPane.setMaxSplitAmount(0.2f);
        splitPane.setSplitAmount(0.2f);

        rootTable.add(splitPane).grow().row();
    }

    public FileChooser getFileChooser() {
        return fileChooser;
    }

    public EditorBrushSettingsView getBrushSettingsView() {
        return editorBrushSettings;
    }

    public MaterialConfigDialog getEditorMaterials() {
        return editorMaterials;
    }

    public CellHierarchyTree getCellTree() {
        return cellHierarchy;
    }

    public void updateDebugs(GLProfiler glProfiler) {
        fpsLabel.setText("fps: " + Gdx.graphics.getFramesPerSecond());
        drawCalls.setText("draws: " + glProfiler.getDrawCalls());
        textures.setText("txbinds: " + glProfiler.getTextureBindings());
        shaders.setText("shaders: " + glProfiler.getShaderSwitches());
    }

    public void showToolWidget(String title, Actor texturePaintView) {
        toolSettingColumn.setActor(texturePaintView);
        toolSettingColumn.right();
    }

    public LibraryTree getLibrary() {
        return libraryHierarchy;
    }
}
