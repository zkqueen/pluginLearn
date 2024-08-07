import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiDirectory
import com.kk.pluginlearn.ui.WidgetType
import java.io.IOException

class FileTemplateGenerator(
    private val input: String,
    private val widgetType: WidgetType,
    private val targetDirectory: PsiDirectory
) {

    fun generateFiles() {
        ApplicationManager.getApplication().runWriteAction {
            try {
                // 生成类名和文件名
                val widgetClassName = if (widgetType == WidgetType.page) "${input}Page" else "${input}Widget"
                val widgetStateClassName = "_${widgetClassName}State"
                val controllerName = "${widgetClassName}Controller"
                val widgetFileName = toSnakeCase(widgetClassName)
                val controllerFileName = "${widgetFileName}_controller"

                // 使用类加载器获取模板文件内容
                val classLoader = this::class.java.classLoader
                val widgetTemplateStream = classLoader.getResourceAsStream("codeTemplate/getx/@widgetFileName.dart")
                val controllerTemplateStream =
                    classLoader.getResourceAsStream("codeTemplate/getx/@controllerFileName.dart")

                if (widgetTemplateStream != null && controllerTemplateStream != null) {
                    val widgetTemplateContent = widgetTemplateStream.bufferedReader().use { it.readText() }
                    val controllerTemplateContent = controllerTemplateStream.bufferedReader().use { it.readText() }

                    // 替换占位符
                    val finalWidgetContent = widgetTemplateContent
                        .replace("@WidgetClassName", widgetClassName)
                        .replace("@WidgetStateClassName", widgetStateClassName)
                        .replace("@ControllerName", controllerName)
                        .replace("@controllerFileName", controllerFileName)

                    val finalControllerContent = controllerTemplateContent
                        .replace("@ControllerName", controllerName)

                    // 生成文件并写入内容
                    val widgetFile = targetDirectory.virtualFile.createChildData(this, "$widgetFileName.dart")
                    val controllerFile = targetDirectory.virtualFile.createChildData(this, "$controllerFileName.dart")

                    widgetFile.getOutputStream(this).use { it.write(finalWidgetContent.toByteArray()) }
                    controllerFile.getOutputStream(this).use { it.write(finalControllerContent.toByteArray()) }

                    println("文件生成成功：${widgetFile.path} 和 ${controllerFile.path}")

                } else {
                    println("无法找到模板文件")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // 将驼峰命名转换为下划线命名
    private fun toSnakeCase(input: String): String {
        return input.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
    }
}
