package jdepend.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import jdepend.framework.util.StringUtil;
import jdepend.framework.util.ThreadPool;
import jdepend.model.Attribute;
import jdepend.model.Component;
import jdepend.model.InvokeItem;
import jdepend.model.JDependUnit;
import jdepend.model.JavaClass;
import jdepend.model.JavaClassRelationItem;
import jdepend.model.JavaPackage;
import jdepend.model.Method;

public class JavaClassUtil {

	/**
	 * 确定指定Class是否属于某一类型
	 * 
	 * @param theJavaClass
	 * @param superClassNames
	 * @return
	 */
	public static boolean isJavaClassType(JavaClass theJavaClass, List<String> superClassNames) {

		Collection<JavaClass> supers = theJavaClass.getSupers();

		for (JavaClass superClass : supers) {
			if (superClassNames.contains(superClass.getName())) {
				return true;
			}
		}
		return false;
	}

	public static Collection<JavaPackage> getJavaPackages(Collection<Component> units) {
		Collection<JavaPackage> javaPackages = new ArrayList<JavaPackage>();
		for (Component unit : units) {
			for (JavaPackage javaPackage : unit.getJavaPackages()) {
				if (!javaPackages.contains(javaPackage)) {
					javaPackages.add(javaPackage);
				}
			}

		}
		return javaPackages;

	}

	public static Collection<JavaClass> getClasses(Collection<Component> units) {
		Collection<JavaClass> javaClasses = new HashSet<JavaClass>();
		for (JDependUnit unit : units) {
			javaClasses.addAll(unit.getClasses());
		}
		return javaClasses;
	}

	public static Collection<JavaClass> getClassesForJavaPackages(Collection<JavaPackage> javaPackages) {
		Collection<JavaClass> javaClasses = new ArrayList<JavaClass>();
		for (JavaPackage javaPackage : javaPackages) {
			javaClasses.addAll(javaPackage.getClasses());
		}
		return javaClasses;
	}

	public static void supplyJavaClassRelationItem(final JavaClassCollection javaClasses) {

		ExecutorService pool = ThreadPool.getPool();

		for (final JavaClass javaClass : javaClasses.getJavaClasses()) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					Iterator<JavaClassRelationItem> it;
					JavaClassRelationItem relationItem;
					JavaClass dependClass;

					it = javaClass.getCaItems().iterator();
					while (it.hasNext()) {
						relationItem = it.next();
						dependClass = javaClasses.getTheClass(relationItem.getCurrentJavaClassPlace(),
								relationItem.getDependJavaClass());
						if (dependClass != null) {
							relationItem.setDepend(dependClass);
							relationItem.setCurrent(javaClass);
						} else {
							it.remove();
						}
					}
					it = javaClass.getCeItems().iterator();
					while (it.hasNext()) {
						relationItem = it.next();
						dependClass = javaClasses.getTheClass(relationItem.getDependJavaClassPlace(),
								relationItem.getDependJavaClass());
						if (dependClass != null) {
							relationItem.setDepend(dependClass);
							relationItem.setCurrent(javaClass);
						} else {
							it.remove();
						}
					}
				}
			});
		}

		ThreadPool.awaitTermination(pool);
	}

	/**
	 * 将JavaClassDetail中的字符串信息填充为对象引用
	 * 
	 * @param javaClasses
	 */
	public static void supplyJavaClassDetail(final JavaClassCollection javaClasses) {
		supplyJavaClassDetailSimple(javaClasses);
		supplyJavaClassDetailMethodInvoke(javaClasses);
	}

	/**
	 * 匹配JavaClass
	 * 
	 * @param pattern
	 * @param javaClass
	 * @return
	 */
	public static boolean match(String pattern, JavaClass javaClass) {
		return match(pattern, javaClass.getName());
	}

	/**
	 * 匹配JavaClass
	 * 
	 * @param pattern
	 * @param className
	 * @return
	 */
	public static boolean match(String pattern, String className) {
		String newPattern = null;
		if (!pattern.endsWith("*")) {
			newPattern = pattern + "*";
		}
		boolean rtn = StringUtil.match(newPattern.toUpperCase(), className.substring(className.lastIndexOf('.') + 1)
				.toUpperCase());
		if (rtn) {
			return true;
		} else {
			return StringUtil.match(pattern.toUpperCase(), className.substring(className.lastIndexOf('.') + 1)
					.toUpperCase());
		}
	}

	/**
	 * 将JavaClassDetail中的字符串信息填充为对象引用
	 * 
	 * @param javaClasses
	 */
	private static void supplyJavaClassDetailSimple(final JavaClassCollection javaClasses) {

		ExecutorService pool = ThreadPool.getPool();

		for (final JavaClass javaClass : javaClasses.getJavaClasses()) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					javaClass.getDetail().supply(javaClasses);
				}
			});
		}

		ThreadPool.awaitTermination(pool);
	}

	/**
	 * 将Method.InvokeItem中的字符串信息填充为对象引用
	 * 
	 * @param javaClasses
	 */
	private static void supplyJavaClassDetailMethodInvoke(final JavaClassCollection javaClasses) {

		ExecutorService pool = ThreadPool.getPool();

		for (final JavaClass javaClass : javaClasses.getJavaClasses()) {
			pool.execute(new Runnable() {
				@Override
				public void run() {

					Iterator<InvokeItem> it;
					InvokeItem invokeItem;

					// 填充Method中的JavaClass
					for (Method method : javaClass.getSelfMethods()) {

						// 填充InvokeItem中的Method
						it = method.getInvokeItems().iterator();
						while (it.hasNext()) {
							invokeItem = it.next();
							invokeItem.setSelf(method);
							if (!invokeItem.supplyMethod(javaClasses)) {
								it.remove();
							}
						}
					}
				}
			});
		}

		ThreadPool.awaitTermination(pool);
	}

	public static void main(String[] args) {
		String pattern = "*controller";

		String className = "com.neusoft.saca.snap.portal.comment.CommentController";

		System.out.print(JavaClassUtil.match(pattern, className));
	}

}
