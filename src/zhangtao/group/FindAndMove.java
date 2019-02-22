package zhangtao.group;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 作者：张涛
 * 复制或移动与匹配规则符合的文件到目标文件
 */
public class FindAndMove {

    //控制塔
    private PrintStream out = System.out;

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }


    private Map<String, String> moved = new Hashtable<>();
    private List<String> ignore = new LinkedList<>();
    private List<String> noRepeat = new LinkedList<>();

    private List<String> wrong = new LinkedList<>();

    private boolean isMove;
    private boolean isAllowRepeat;


    private File ffrom;
    private File fto;
    private File fpattern;

    private List<Pattern> patterns;

    private String repeat = "-repeat";

    public FindAndMove(String from, String to, String patterPath, boolean isMove, boolean isAllowRepeat) {
        this.isMove = isMove;

        ffrom = new File(from);
        fto = new File(to);
        fpattern = fto.toPath().resolve(patterPath).toFile();
        this.isAllowRepeat = isAllowRepeat;
        patterns = new LinkedList<>();
    }

    public void execute() {
        findAndMove();
    }

    private void findAndMove() {
        //init
        if (!ffrom.exists()) {
            out.println("源文件不存在！");
            return;
        }
        if (!fto.exists() || !fto.isDirectory()) {
            if (!fto.mkdirs()) {
                out.println("创建目标文件夹失败！");
                return;
            }
        }
        if (!fpattern.exists() || fpattern.isDirectory()) {
            out.println("匹配规则不存在！");
            return;
        } else {
            try (Scanner scanner = new Scanner(
                    new BufferedInputStream(
                            new FileInputStream(fpattern)
                    )
            )) {
                while (scanner.hasNextLine()) {
                    String s = scanner.nextLine();
                    if (s != null && !s.trim().equals(""))
                        patterns.add(compline(s));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                out.println("匹配规则解析失败！");
                return;
            }
        }
        if (patterns.size() == 0) {
            out.println("匹配规则为空！");
            return;
        }
        if(ffrom.getAbsolutePath().indexOf(fto.getAbsolutePath())==0){
            out.println("要保存的文件夹是目标文件夹的子文件夹，会造成错误！");
            return;
        }
        //开始处理
        if (ffrom.isDirectory()) {
            checkDirection(ffrom);
        } else {
            try {
                moveOrCopyFle(ffrom);
            } catch (IOException e) {
                //不可能发生
                e.printStackTrace();
                wrong.add(ffrom.getAbsolutePath());
                return;
            }
        }
        String result = "处理结果";
        while (Files.exists(fto.toPath().resolve(result + ".txt"))) {
            result += repeat;
        }
        try (
                PrintWriter printWriter = new PrintWriter(
                        new BufferedOutputStream(
                                new FileOutputStream(fto.toPath().resolve(result + ".txt").toFile())
                        )
                )) {
            printWriter.println("以下文件处理失败：" + wrong.size());
            wrong.forEach(printWriter::println);
            if (!isAllowRepeat) {
                printWriter.println("以下文件重名，由于参数-r false，已被忽略");
                noRepeat.forEach(printWriter::println);
            }
            printWriter.println("\n\n以下文件正常处理：" + moved.size());
            moved.forEach((key, value) -> printWriter.println(key + "   源文件：   " + value));
            printWriter.println("\n\n以下文件不符合匹配规则，已被忽略：" + ignore.size());
            ignore.forEach(printWriter::println);

            out.println("\n\n处理完毕，详细处理报告已被保存在：" + fto.toPath().resolve(result + ".txt").toFile().getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkDirection(File from) {
        File[] files = from.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                checkDirection(file);
            } else {
                try {
                    moveOrCopyFle(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    wrong.add(file.getAbsolutePath());
                }
            }
        }
    }

    private void moveOrCopyFle(File from) throws IOException {
        String name = from.getName();
        for (Pattern p : patterns) {
            if (p.matcher(name).matches()) {
                Path toPath = fto.toPath().resolve(from.getName());
                //不允许重名
                if (!isAllowRepeat && Files.exists(toPath)) {
                    noRepeat.add(from.getAbsolutePath());
                    return;
                }
                boolean isRepeat = false;
                //重名处理
                while (Files.exists(toPath)) {
                    String originName = toPath.toFile().getName();
                    if (originName.contains(".")) {
                        String first = originName.substring(0, originName.lastIndexOf("."));
                        String last = originName.substring(originName.lastIndexOf("."));
                        originName = first + repeat + last;
                    } else {
                        originName += repeat;
                    }
                    toPath = fto.toPath().resolve(originName);
                    isRepeat = true;
                }

                if (isMove) {
                    Files.move(from.toPath(), toPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(from.toPath(), toPath, StandardCopyOption.REPLACE_EXISTING);
                }
                //记录
                String status;
                if (isRepeat) {
                    status = "重名，以更名：" + toPath.toFile().getName();
                } else {
                    status = "正常处理：" + toPath.toFile().getName();
                }
                moved.put(status, from.getAbsolutePath());
                out.println(from.getAbsolutePath() + status);
                return;
            }
        }
        ignore.add(from.getAbsolutePath());
    }

    private static Pattern compline(String s) {
        s = s.replace('.', '#');
        s = s.replaceAll("#", "\\\\.");
        s = s.replace('*', '#');
        s = s.replaceAll("#", ".*");
        s = s.replace('?', '#');
        s = s.replaceAll("#", ".?");
        s = "^" + s + "$";
        Pattern p = Pattern.compile(s);
        return p;
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            new FindAndMove(args[0], args[1], "pattern.txt", true, true).execute();
        } else if (args.length == 4 || args.length == 6 || args.length == 8) {
            String patternPath = "pattern.txt";
            boolean isMove = true;
            boolean isRepeat = true;
            for (int i = 1; i < args.length / 2; i++) {
                String order = args[2 * i];
                String value = args[2 * i + 1];
                if ("-l".equals(order)) {
                    patternPath = value;
                } else if ("-r".equals(order)) {
                    if (value.equals("true")) {
                        isRepeat = true;
                    } else if (value.equals("false")) {
                        isRepeat = false;
                    } else {
                        System.out.println("参数错误！true|false");
                        return;
                    }
                } else if ("-m".equals(order)) {
                    if (value.equals("true")) {
                        isMove = true;
                    } else if (value.equals("false")) {
                        isMove = false;
                    } else {
                        System.out.println("参数错误！true|false");
                        return;
                    }
                } else {
                    System.out.println("参数错误！！  " + order);
                    return;
                }
            }
            new FindAndMove(args[0], args[1], patternPath, isMove, isRepeat).execute();
        } else {
            System.out.println("当前参数数目：" + args.length);
            for (String arg : args) {
                System.out.println(arg);
            }
            System.out.println("帮助：");
            System.out.println("前两个参数：");
            System.out.println("from path : 来源");
            System.out.println("to path : 目的文件夹");
            System.out.println("path不要有父子关系！！！！！");
            System.out.println("可选参数：");
            System.out.println("-m : true|false 是不是移动，默认：true");
            System.out.println("-r : true|false 是不是允许重名，true时会自动重命名，false时忽略重名文件，默认：true");
            System.out.println("-l : 匹配规则文件，必须有，默认为pattern.txt,相对于 to path");
            System.out.println("实例：");
            System.out.println("java -jar FindAndMove.jar /home/from /home/to     (说明：/home/to下必须有pattern.txt的匹配规则)");
            System.out.println("java -jar FindAndMove.jar /home/from /home/to -m false -l pattern.txt");
            System.out.println("java -jar FindAndMove.jar /home/from /home/to -m false -l pattern.txt -r false");
        }
    }
}