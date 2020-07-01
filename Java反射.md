# Java 反射的应用

## 利用反射获取类中私有的成员变量

这个方法可能不是能经常使用到，但是却是一个很重要的途径。
最常见的，它可以帮助我们直接改写、继承一些第三方依赖的类来扩展成自己想要的类，从而实现自己的需求。

我们知道，在父类中，如果一些属性被标记为 `private` 或者默认的 `package` 等权限修饰符时，作为子类是无法获取、使用到这些变量的。
这还算是好的，因为你可能可以在子类中写出这些变量的类名。
更甚的，对于内部类，你既无法获取使用，也不能在子类中写出来，因为它是无法被识别的。
可以看看以下的例子：
```java
package com.thireparty.a;
public class A {
    private AParam aParam;
    
    // A 的内部类，在 B 中是无法识别的，因为它在第三方依赖中并不公开
    enum AEnum{A_ENUM_1, A_ENUM_2}
    private final AEnum aEnum;
    
    public A() {this.aEnum = AEnum.A_ENUM_1;}

    public A(AEnum aEnum) {
        this.aEnum = aEnum;
    }
}

package com.thireparty.a;
public class AParam {
    //...
}

package com.linxl.b;
import com.thireparty.a.*;
public class B extends A {

    // 这里 B 明显不能直接获取到 A 的成员变量 aParam
    // 但 B 可以写出 aParam 的类，因为 AParam 并不是内部类，是可以被识别的
    private AParam aParamForB; 

    // 获取父类 A 中的 aParam
    try {
        Field aParamField = A.class.getDeclaredField("aParam");
        // 开通获取
        aParamField.setAccessible(true);
        // 获取到父类 A 中的 aParam，此时如果需要可以强制转换为对应的类型 AParam
        AParam aParam = (AParam) aParamField.get(this);
        //... 如果需要甚至可以对 aParam 进行一些处理
        // 处理完之后还可以将它设置回父类 A 中，A 中的 aParam 值也会实时发生变化
        aParamField.set(this, aParam);
    } catch (Exception e) {
        throw new IllegalStateException("get / set aParam faild.", e);
    }

    // 获取父类 A 中的内部类 AEnum，甚至获取其中的值
    // 获取父类 A 中声明的类，这里就一个内部类，所以获取的 declaredClasses 就只有一个
    final Class<?> declaredClasse = A.class.getDeclaredClasses()[0];
    final Field field = declaredClasse.getDeclareField("A_ENUM_2");
    field.setAccessible(true);
    // 这里无法进行强转了，因为你写出的 AEnum 是无法被识别的
    final Object o = field.get(this);
    
    // 我们还可以使用这个获取到的内部类的值，来调用父类 A 的一个构造方法
    // 获取父类 A 的构造方法，并指定是拥有 o.getClass() 类型的构造方法，这样返回给我们的构造方法就会是 public A(AEnum aEnum)
    // 如果指定的是 Void.class，那么返回的构造方法就是 public A()
    final Constructor<A> constructor = A.class.getConstructor(o.getClass());
    // 这样就可以通过 A 的构造方法新建一个实力出来
    A a = constructor.newInstance(o);
}
```
这个的实际使用，是在扩展 logback 1.2.3 版本中的日志滚动策略。
这是为了规避这个版本的一个硬编码问题，导致 logback 无法删除超过 3 位数编号的日志，从而最终导致日志总容量挤爆服务器容量。
请参考如下代码：
- LogbackFileNamePartten

继承 FileNamePattern 类来规避 logback 的硬编码错误。
```java
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * 用于修复 logback 1.2.3 版本中 FileNamePattern#toRegexForFixedDate()、toRegex 正则表达式的错误导致日志编号在超过 3 位数后无法被删除的问题
 */
public class LogbackFileNamePartten extends FileNamePattern  {

    public LogbackFileNamePartten(FileNamePattern pattern) {
        super(pattern.getPattern(),  pattern.getContext());
    }

    @Override
    public String toRegexForFixedDate(Date date) {
        StringBuilder buf = new StringBuilder();
        Converter<Object> p = getHeadTokenConverter();
        while (p != null) {
            if (p instanceof LiteralConverter) {
                buf.append(p.convert(null));
            } else if (p instanceof IntegerTokenConverter) {
                buf.append("(\\d+?)");
            } else if (p instanceof DateTokenConverter) {
                buf.append(p.convert(date));
            }
            p = p.getNext();
        }
        return buf.toString();
    }

    @Override
    public String toRegex() {
        StringBuilder buf = new StringBuilder();
        Converter<Object> p = getHeadTokenConverter();
        while (p != null) {
            if (p instanceof LiteralConverter) {
                buf.append(p.convert(null));
            } else if (p instanceof IntegerTokenConverter) {
                buf.append("\\d+?");
            } else if (p instanceof DateTokenConverter) {
                DateTokenConverter<Object> dtc = (DateTokenConverter<Object>) p;
                buf.append(dtc.toRegex());
            }
            p = p.getNext();
        }
        return buf.toString();
    }

    public Converter<Object> getHeadTokenConverter() {
        try {
            final Field field = FileNamePattern.class.getDeclaredField("headTokenConverter");
            field.setAccessible(true);
            return (Converter<Object>) field.get(this);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace file name partten failed, can not get headTokenConverter.", e);
        }
    }
}
```
- SizeAndTimeRollingPolicy

继承 TimeBasedRollingPolicy 来自定义自己的日志滚动策略，其实就是重写 `TimeBasedRollingPolicy#start()` 方法。

重写它的主要原因是将其中使用到 `FileNamePattern` 的地方都换成自定义的 `LogbackFileNamePartten`。

重写的 `start()` 方法代码其实就是 SizeAndTimeBasedRollingPolicy（logback 官方的基于日志大小和时间的滚动策略）、TimeBasedRollingPolicy 和 RollingPolicyBase 中的 start() 代码。

因为上述代码中存在了大量的私有成员变量，继承的子类中只能通过本章节介绍的方法来获取、使用、并回设到父类中，以继续让父类的逻辑得以正常的运行。
```java
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.*;
import ch.qos.logback.core.rolling.helper.*;
import ch.qos.logback.core.util.FileSize;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Date;

import static ch.qos.logback.core.CoreConstants.UNBOUND_HISTORY;

public class SizeAndTimeRollingPolicy<E> extends TimeBasedRollingPolicy<E> {

    FileSize maxFileSize;

    static final String FNP_NOT_SET = "The FileNamePattern option must be set before using TimeBasedRollingPolicy. ";

    @Override
    public void start() {

        final SizeAndTimeBasedFNATP<E> sizeAndTimeBasedFNATP;
        try {
            final Class<?> declaredClass = SizeAndTimeBasedFNATP.class.getDeclaredClasses()[0];
            final Field field = declaredClass.getDeclaredField("EMBEDDED");
            field.setAccessible(true);
            final Object embedded = field.get(this);

            final Constructor<SizeAndTimeBasedFNATP> constructor = SizeAndTimeBasedFNATP.class.getConstructor(embedded.getClass());
            sizeAndTimeBasedFNATP = constructor.newInstance(embedded);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace failed, can not new sizeAndTimeBasedFNATP.", e);
        }

        if (maxFileSize == null) {
            addError("maxFileSize property is mandatory.");
            return;
        } else {
            addInfo("Archive files will be limited to [" + maxFileSize + "] each.");
        }

        sizeAndTimeBasedFNATP.setMaxFileSize(maxFileSize);
        TimeBasedFileNamingAndTriggeringPolicy<E> timeBasedFileNamingAndTriggeringPolicy = sizeAndTimeBasedFNATP;

        if (!isUnboundedTotalSizeCap() && totalSizeCap.getSize() < maxFileSize.getSize()) {
            addError("totalSizeCap of [" + totalSizeCap + "] is smaller than maxFileSize [" + maxFileSize + "] which is non-sensical");
            return;
        }

        // set the LR for our utility object
        final RenameUtil renameUtil;
        try {
            final Field field = TimeBasedRollingPolicy.class.getDeclaredField("renameUtil");
            field.setAccessible(true);
            renameUtil = (RenameUtil) field.get(this);
            renameUtil.setContext(this.context);
            field.set(this, renameUtil);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace failed, can not set renameUtil.", e);
        }

        // find out period from the filename pattern
        if (fileNamePatternStr != null) {
            try {
                final Field field = RollingPolicyBase.class.getDeclaredField("fileNamePattern");
                field.setAccessible(true);
                field.set(this, new LogbackFileNamePartten(new FileNamePattern(fileNamePatternStr, this.context)));
            } catch (Exception e) {
                throw new IllegalStateException("Logback replace file name partten failed, can not set fileNamePattern.", e);
            }
//            fileNamePattern = new FileNamePattern(fileNamePatternStr, this.context);
            determineCompressionMode();
        } else {
            addWarn(FNP_NOT_SET);
            addWarn(CoreConstants.SEE_FNP_NOT_SET);
            throw new IllegalStateException(FNP_NOT_SET + CoreConstants.SEE_FNP_NOT_SET);
        }

        final Compressor compressor = new Compressor(compressionMode);
        compressor.setContext(context);
        try {
            final Field field = TimeBasedRollingPolicy.class.getDeclaredField("compressor");
            field.setAccessible(true);
            field.set(this, compressor);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace failed, can not set compressor.", e);
        }

        // wcs : without compression suffix
        FileNamePattern fileNamePatternWithoutCompSuffix = new LogbackFileNamePartten(
                new FileNamePattern(Compressor.computeFileNameStrWithoutCompSuffix(fileNamePatternStr, compressionMode), this.context));
        try {
            final Field field = TimeBasedRollingPolicy.class.getDeclaredField("fileNamePatternWithoutCompSuffix");
            field.setAccessible(true);
            field.set(this, fileNamePatternWithoutCompSuffix);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace failed, can not set fileNamePatternWithoutCompSuffix.", e);
        }

        addInfo("Will use the pattern " + fileNamePatternWithoutCompSuffix + " for the active file");

        if (compressionMode == CompressionMode.ZIP) {
            String zipEntryFileNamePatternStr = transformFileNamePattern2ZipEntry(fileNamePatternStr);
            try {
                final Field field = RollingPolicyBase.class.getDeclaredField("zipEntryFileNamePattern");
                field.setAccessible(true);
                field.set(this, new LogbackFileNamePartten(new FileNamePattern(zipEntryFileNamePatternStr, context)));
            } catch (Exception e) {
                throw new IllegalStateException("Logback replace file name partten failed, can not set fileNamePattern.", e);
            }
        }

        if (timeBasedFileNamingAndTriggeringPolicy == null) {
            timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<E>();
        }
        timeBasedFileNamingAndTriggeringPolicy.setContext(context);
        timeBasedFileNamingAndTriggeringPolicy.setTimeBasedRollingPolicy(this);
        timeBasedFileNamingAndTriggeringPolicy.start();

        try {
            final Field field = TimeBasedRollingPolicy.class.getDeclaredField("timeBasedFileNamingAndTriggeringPolicy");
            field.setAccessible(true);
            field.set(this, timeBasedFileNamingAndTriggeringPolicy);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace failed, can not set timeBasedFileNamingAndTriggeringPolicy.", e);
        }

        if (!timeBasedFileNamingAndTriggeringPolicy.isStarted()) {
            addWarn("Subcomponent did not start. TimeBasedRollingPolicy will not start.");
            return;
        }

        // the maxHistory property is given to TimeBasedRollingPolicy instead of to
        // the TimeBasedFileNamingAndTriggeringPolicy. This makes it more convenient
        // for the user at the cost of inconsistency here.
        int maxHistory;
        try {
            final Field field = TimeBasedRollingPolicy.class.getDeclaredField("maxHistory");
            field.setAccessible(true);
            maxHistory = (int) field.get(this);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace file name partten failed, can not get maxHistory.", e);
        }

        if (maxHistory != UNBOUND_HISTORY) {
            ArchiveRemover archiveRemover = timeBasedFileNamingAndTriggeringPolicy.getArchiveRemover();
            archiveRemover.setMaxHistory(maxHistory);
            archiveRemover.setTotalSizeCap(totalSizeCap.getSize());
            try {
                final Field field = TimeBasedRollingPolicy.class.getDeclaredField("archiveRemover");
                field.setAccessible(true);
                field.set(this, archiveRemover);
            } catch (Exception e) {
                throw new IllegalStateException("Logback replace file name partten failed, can not set archiveRemover.", e);
            }

            boolean cleanHistoryOnStart;
            try {
                final Field field = TimeBasedRollingPolicy.class.getDeclaredField("cleanHistoryOnStart");
                field.setAccessible(true);
                cleanHistoryOnStart = (boolean) field.get(this);
            } catch (Exception e) {
                throw new IllegalStateException("Logback replace file name partten failed, can not get cleanHistoryOnStart.", e);
            }

            if (cleanHistoryOnStart) {
                addInfo("Cleaning on start up");
                Date now = new Date(timeBasedFileNamingAndTriggeringPolicy.getCurrentTime());

                try {
                    final Field field = TimeBasedRollingPolicy.class.getDeclaredField("cleanUpFuture");
                    field.setAccessible(true);
                    field.set(this, archiveRemover.cleanAsynchronously(now));
                } catch (Exception e) {
                    throw new IllegalStateException("Logback replace file name partten failed, can not set cleanUpFuture.", e);
                }
            }
        } else if (!isUnboundedTotalSizeCap()) {
            addWarn("'maxHistory' is not set, ignoring 'totalSizeCap' option with value [" + totalSizeCap + "]");
        }


        try {
            final Field field = RollingPolicyBase.class.getDeclaredField("started");
            field.setAccessible(true);
            field.set(this, true);
        } catch (Exception e) {
            throw new IllegalStateException("Logback replace file name partten failed, can not start.", e);
        }
    }

    private String transformFileNamePattern2ZipEntry(String fileNamePatternStr) {
        String slashified = FileFilterUtil.slashify(fileNamePatternStr);
        return FileFilterUtil.afterLastSlash(slashified);
    }

    public void setMaxFileSize(FileSize aMaxFileSize) {
        this.maxFileSize = aMaxFileSize;
    }

    @Override
    public String toString() {
        return "com.mastercard.cme.caas.service.log.SizeAndTimeRollingPolicy@"+this.hashCode();
    }
}
```

通过以上代码，在 logback 配置文件中，只需要将日志滚动策略替换成自己编写的 SizeAndTimeRollingPolicy 类即可：
```xml
<rollingPolicy class="包名.SizeAndTimeRollingPolicy">
...
</rollingPolicy>
```