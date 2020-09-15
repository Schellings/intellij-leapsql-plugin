package com.plugins.mybaitslog.action;

import com.google.common.base.Strings;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.project.Project;
import com.plugins.mybaitslog.icons.Icons;
import com.plugins.mybaitslog.util.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.Objects;


/**
 * 控制台 右键 启动Sql格式化输出窗口
 *
 * @author lk
 * @version 1.0
 * @date 2020/8/23 17:14
 */
public class RestoreSqlForSelection extends AnAction {


    public RestoreSqlForSelection() {
        super(Icons.MyBatisIcon);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        CaretModel caretModel = Objects.requireNonNull(e.getData(LangDataKeys.EDITOR)).getCaretModel();
        Caret currentCaret = caretModel.getCurrentCaret();
        String selectedText = currentCaret.getSelectedText();
        if(StringUtils.isEmpty(selectedText)){
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable clipTf = clipboard.getContents(null);
            if (clipTf != null) {
                // 检查内容是否是文本类型
                if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        selectedText = (String) clipTf
                                .getTransferData(DataFlavor.stringFlavor);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        ConfigUtil.setShowMyBatisLog(project);
        final String preparing = ConfigUtil.getPreparing(project);
        final String parameters = ConfigUtil.getParameters(project);
        if (StringUtils.isNotEmpty(selectedText)) {
            //分割每一行
            String[] selectedRowText = selectedText.split(parameters);
            if (isKeyWord(project, selectedText, selectedRowText, preparing, parameters)) {
                setFormatSelectedText(project, selectedRowText, preparing, parameters);
            }
        }
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        this.getTemplatePresentation().setEnabled(true);
    }

    /**
     * 是否存在关键字文本
     *
     * @param project         项目
     * @param selectedText    文本
     * @param selectedRowText 文本
     * @param preparing       关键字
     * @param parameters      关键字
     */
    private boolean isKeyWord(Project project, String selectedText, String[] selectedRowText, String preparing, String parameters) {
        if (selectedRowText.length >= 2) {
            return true;
        }
        PrintlnUtil.println(project, "解析错误", ConsoleViewContentType.ERROR_OUTPUT,true);
        PrintlnUtil.println(project, "parameters :"+ parameters, ConsoleViewContentType.ERROR_OUTPUT,true);
        for (int i = 0; i < selectedRowText.length; i++) {
            PrintlnUtil.println(project," line" + i + " :"+ selectedRowText[i], ConsoleViewContentType.USER_INPUT);
        }

        return false;
    }

    /**
     * 设置显示的文本,局部
     *
     * @param project         项目
     * @param selectedRowText 文本
     * @param preparing       关键字
     * @param parameters      关键字
     */
    private void setFormatSelectedText(Project project, String[] selectedRowText, String preparing, String parameters) {

        String sqls = selectedRowText[0].trim().substring(selectedRowText[0].indexOf(":") + 1);
        if("".equals(sqls.trim())){
            sqls = selectedRowText[0].trim();
        }
        sqls = sqls.replace("?","%s");
        String params = selectedRowText[1].trim();
        params = params.replace("[","").replace("]","");
        String[] paramsArray = params.split(",");
        String finalSQL = String.format(sqls,paramsArray);
        PrintlnUtil.println(project, "--- \n\n" + "解析成功，已复制到剪贴板" + "\n\n --- \n", ConsoleViewContentType.USER_INPUT);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(finalSQL);
        clipboard.setContents(tText,null);
    }

}