package z;

public class DummyProcess {
    public static void main(String[] args) throws InterruptedException {
        if(args.length==1){
            int number = Integer.parseInt(args[0]);
            for(int x=0;x<number;++x){
                Thread.sleep(300);
                System.out.println("STD_IN:"+x);
            }
            for(int x=0;x<number;++x){
                Thread.sleep(300);
                System.err.println("STD_ERR:"+x);
            }
        }else{
            System.out.println("Accept exactly 1 number");
        }
    }
}
