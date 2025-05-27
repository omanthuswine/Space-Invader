# Space Shooter Game - JavaFX

Chào mừng bạn đến với Space Shooter, một trò chơi bắn tàu vũ trụ cổ điển được xây dựng bằng JavaFX, nổi bật với hệ thống AI điều khiển tàu người chơi đầy thử thách!

## Giới Thiệu Chung

Space Shooter là một trò chơi arcade theo chiều dọc, nơi người chơi (hoặc AI) điều khiển một phi thuyền chiến đấu, đối mặt với các làn sóng kẻ địch và những con trùm (boss) hùng mạnh. Mục tiêu là sống sót càng lâu càng tốt, tiêu diệt nhiều kẻ địch và đạt điểm số cao nhất. Trò chơi đặc biệt với một AI Player Controller có khả năng tự né đạn, chọn mục tiêu, tấn công.

## Gameplay

### Mục Tiêu Trò Chơi
* Tiêu diệt các phi thuyền địch và boss để ghi điểm.
* Sống sót qua các đợt tấn công (wave) ngày càng khó của kẻ địch.
* Thu thập các vật phẩm tăng sức mạnh (power-up) để nâng cao khả năng chiến đấu.
* Đạt được điểm số cao nhất và đánh bại trùm cuối cùng để giành chiến thắng.

### Điều Khiển
* **Di chuyển:** Sử dụng các phím `A`, `W`, `S`, `D` hoặc các phím mũi tên (`LEFT`, `UP`, `DOWN`, `RIGHT`).
* **Bắn:** Nhấn phím `SPACE`.
* **Chế độ AI:** Nhấn phím `SHIFT` để bật/tắt chế độ AI điều khiển tàu của bạn.

### Kẻ Địch
* **Kẻ Địch Thường (Enemy):** Các phi thuyền cơ bản, di chuyển xuống dưới và có thể bắn đạn về phía người chơi. Nếu chúng chạm tới cuối màn hình, người chơi sẽ mất một mạng.
* **Trùm (Boss Enemy):** Những kẻ địch đặc biệt mạnh với lượng máu lớn, đa dạng về hình dạng và kiểu tấn công. Chúng có các kiểu di chuyển phức tạp (zigzag, hình tròn) và có thể bắn ra nhiều loại đạn theo cácรูปแบบ khác nhau (ví dụ: bắn vòng tròn, bắn có chủ đích). Có cả những con trùm đứng yên với hỏa lực mạnh.

### Hệ Thống Wave và Độ Khó
* Trò chơi được chia thành các "wave" (đợt tấn công).
* Boss sẽ xuất hiện khi người chơi đạt đủ điểm số hoặc hoàn thành một wave nhất định.
* Độ khó của game (ví dụ: tần suất xuất hiện của kẻ địch) sẽ tăng dần theo wave và điểm số của người chơi.

### Vật Phẩm (Power-ups)
Người chơi có thể thu thập các vật phẩm sau để tăng lợi thế:
* **Health Pack (Gói Máu):** Tăng số mạng hiện tại của người chơi (tối đa 5 mạng).
* **Shield (Khiên):** Tạo một lớp khiên bảo vệ tạm thời, giúp người chơi miễn nhiễm sát thương.
* **Triple Shot (Bắn Ba Tia):** Cho phép tàu người chơi bắn ra ba tia đạn cùng lúc trong một khoảng thời gian.

### Điểm Số và Mạng Sống
* Người chơi ghi điểm bằng cách tiêu diệt kẻ địch và boss. Boss mang lại nhiều điểm hơn.
* Người chơi bắt đầu với một số mạng nhất định (mặc định là 3).
* Mất mạng khi va chạm với kẻ địch, đạn địch, hoặc để kẻ địch thường vượt qua cuối màn hình.
* Trò chơi kết thúc (Game Over) khi người chơi mất hết mạng.
* Người chơi chiến thắng khi đánh bại được trùm cuối ở Wave 3.

## AI Player Controller (Trình Điều Khiển AI)

Một trong những điểm đặc biệt của trò chơi này là hệ thống AI có khả năng điều khiển tàu của người chơi. AI được thiết kế với các khả năng sau:
* **Phân Tích Mối Đe Dọa:** Liên tục quét môi trường để phát hiện đạn địch và thân tàu địch có khả năng gây nguy hiểm.
* **Né Tránh Thông Minh:**
    * Dự đoán đường bay của đạn và chủ động di chuyển đến vị trí an toàn. AI cố gắng duy trì một "vùng an toàn" (bán kính 80px) xung quanh mình, không có đạn địch trong vòng 1.5 giây tới.
    * Thực hiện các cú né tránh chiến lược với nhiều tầng fallback: né lý tưởng, né hoảng loạn (khi bị dồn vào thế khó), và né vi mô.
    * Né tránh va chạm trực tiếp với thân tàu địch.
* **Tấn Công Hiệu Quả:**
    * Xác định mục tiêu (ưu tiên boss, sau đó đến các kẻ địch thường dựa trên điểm số và vị trí).
    * Tự động căn chỉnh vị trí để bắn trúng mục tiêu.
    * Tốc độ bắn được kiểm soát bởi cooldown (hiện tại là 0.02 giây giữa các lần cố gắng bắn).
* **Thu Thập Vật Phẩm:** Chủ động di chuyển đến các vật phẩm có lợi nếu đường đi an toàn.
* **Duy Trì Vị Trí Ưa Thích:** Cố gắng giữ tàu ở một vị trí tối ưu ở cuối màn hình.
* **Dịch Chuyển Tức Thời (Teleport):** Trong trường hợp cực kỳ nguy hiểm và không còn đường né tránh, AI có khả năng dịch chuyển tức thời đến một vị trí an toàn gần nhất có thể. Đây là biện pháp cuối cùng để bảo toàn mạng sống.
* **Phản Ứng Nhanh:** Các độ trễ trong việc ra quyết định được tối ưu hóa để AI phản ứng cực nhanh với các thay đổi trên màn hình.

## Cách Chơi
1.  Khởi chạy trò chơi.
2.  Từ menu chính, chọn "START" để bắt đầu.
3.  Sử dụng các phím điều khiển (mũi tên hoặc A,W,S,D) để di chuyển tàu của bạn.
4.  Nhấn `SPACE` để bắn tiêu diệt kẻ thù.
5.  Cố gắng sống sót, thu thập vật phẩm và đạt điểm cao!
6.  Nhấn `SHIFT` bất cứ lúc nào để xem AI trình diễn kỹ năng né đạn và chiến đấu.

## Thông Tin Kỹ Thuật (Tùy chọn)
* **Ngôn ngữ:** Java
* **Thư viện đồ họa:** JavaFX
* **Mô hình:** Lập trình hướng đối tượng (OOP)

---
Chúc bạn có những giờ phút giải trí vui vẻ với Space Shooter!