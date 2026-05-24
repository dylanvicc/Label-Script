package com.ls.api.engine.ast.node.impl;

import java.util.List;

import com.ls.api.engine.ast.node.Node;

public class DocumentNode extends Node {

    private final int width;
    private final int height;
    private final int dpi;
    private final List<StatementNode> statements;

    public DocumentNode(int width, int height, int dpi, List<StatementNode> statements) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
        this.statements = statements;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDpi() {
        return dpi;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }
}
