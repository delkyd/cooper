package jdepend.ui.util;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jdepend.core.command.CommandAdapterMgr;
import jdepend.framework.exception.JDependException;
import jdepend.framework.util.FileUtil;
import jdepend.model.JDependUnitMgr;
import jdepend.model.result.AnalysisResult;
import jdepend.ui.JDependCooper;

public class AnalysisResultExportUtil {

	public static void exportResult(JDependCooper frame, AnalysisResult result) throws JDependException, IOException {

		JFileChooser jFileChooser = getJFileChooser();

		int rtn = jFileChooser.showSaveDialog(null);

		if (rtn == JFileChooser.APPROVE_OPTION) {
			if (result != null) {
				File f = jFileChooser.getSelectedFile();
				String fileName = f.getAbsolutePath();
				if (!fileName.endsWith(".cpr")) {
					fileName += ".cpr";
				}
				FileUtil.saveFile(fileName, result.getBytes());
				JOptionPane.showMessageDialog(frame, "导出分析结果成功。", "alert", JOptionPane.INFORMATION_MESSAGE);
			} else {
				throw new JDependException("未存储结果信息");
			}
		}
	}

	public static void importResult(final JDependCooper frame) throws JDependException, IOException,
			ClassNotFoundException {

		JFileChooser jFileChooser = getJFileChooser();

		int rtn = jFileChooser.showSaveDialog(null);

		if (rtn == JFileChooser.APPROVE_OPTION) {
			File f = jFileChooser.getSelectedFile();
			String fileName = f.getAbsolutePath();
			byte[] data = FileUtil.readFile(fileName);

			AnalysisResult result = AnalysisResult.create(data);
			// 清空历史
			frame.clearPriorResult();
			// 显示结果
			JDependUnitMgr.getInstance().setResult(result);
			CommandAdapterMgr.setCurrentGroup(result.getRunningContext().getGroup());
			CommandAdapterMgr.setCurrentCommand(result.getRunningContext().getCommand());
			frame.getResultPanel().showResults();
			// 刷新TODOList
			new Thread() {
				@Override
				public void run() {
					try {
						frame.getPropertyPanel().getToDoListPanel().refresh();
					} catch (JDependException e) {
						e.printStackTrace();
						frame.getResultPanel().showError(e);
					}
				}
			}.start();
		}

	}

	private static JFileChooser getJFileChooser() {
		JFileChooser jFileChooser = new JFileChooser(System.getProperty("user.home"));
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "数据文件(*.cpr)";
			}

			@Override
			public boolean accept(File f) {
				if (f.getName().endsWith(".cpr")) {
					return true;
				} else {
					return false;
				}
			}
		});
		return jFileChooser;
	}

}