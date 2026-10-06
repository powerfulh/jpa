public class FastTest {

	public static void main(String[] args) {
        String s = "주요 고객·사업 기반: CJ그룹 계열사 및 외부 기업";
		char[] cs = s.toCharArray();
//		System.out.println(cs);
		for(var item: cs) {
			System.out.println(item);
			System.out.println(item == ' ');
		}
    }
}
