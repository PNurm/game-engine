package com.gengine.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gengine.editor.ui.views.*;
import com.gengine.editor.ui.widgets.SceneRenderWidget;
import com.gengine.editor.ui.widgets.SplitPane;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;


public class UI extends Stage {

    private final VisTable rootTable;
    //private final SplitPane splitPane;
    private final EditorBrushSettingsView editorBrushSettings;
    private final EditorMaterials editorMaterials;
    private final EditorRegionNodeTree editorNodeTree;
    private final VisLabel fpsLabel;
    private final VisLabel drawCalls;
    private final VisLabel shaders;
    private final VisLabel textures;
    private final Cell toolSettingColumn;

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
        editorMaterials = new EditorMaterials();


        fileChooser = new FileChooser(FileChooser.Mode.OPEN);

        rootTable.add(menuBar.getTable()).fillX().expandX().top().row();
        rootTable.add(toolbar.getRoot()).fillX().expandX().row();

        //rootTable.add(toolSettingBar).fillX().growX().row();
        toolSettingColumn = toolbar.getRight().add();

        editorNodeTree = new EditorRegionNodeTree();
        editorBrushSettings = new EditorBrushSettingsView();

        VisTable debuInfo = new VisTable();
        debuInfo.add(fpsLabel = new VisLabel("FPS")).pad(5);
        debuInfo.add(drawCalls = new VisLabel("Draws")).pad(5);
        debuInfo.add(shaders = new VisLabel("Shaders")).pad(5);
        debuInfo.add(textures = new VisLabel("Textures")).pad(5);
        sceneWidget.add(debuInfo);

        SplitPane splitPane = new SplitPane(editorNodeTree, sceneWidget, false);
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

    public EditorMaterials getEditorMaterials() {
        return editorMaterials;
    }

    public EditorRegionNodeTree getCellTree() {
        return editorNodeTree;
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
}
