# < Smart Bus System > Capston Design 1 

<p> Environment : Vi Editor in Raspberry Pi/Linux, Android Studio in Windows </p>
<p> Language : C, Java, Python </p>

<br><br>

<p> 개발 배경 </p>
- 버스나 지하철을 이용할 시 카드/스마트폰을 태그하여 요금을 지불함 (NFC) -> 카드/폰을 꺼내야 하는 불편함과 가끔 잊어버릴 때도 있음
- 버스에서 내릴 때 벨을 눌러야 하는 점 -> 버스가 이동 중일 때 일어서야 하는 위험함
- 도착 정류장 -> 딴짓하다가 내려야 할 곳을 놓칠 경우, 초행길에는 매 정류장마다 주의를 기울일 필요가 있음

<p> 기대 효과 </p> 
- 본인과 카드를 인증하기 때문에 할인 카드 부정 사용 감소 가능
- 버스 출/입구를 통과하지 않으면 결제 불가 -> 하차 전 태그 방지 가능
- 카메라(얼굴 인식) 이용 -> 상습 부정 승차 인물 확인 가능
<br>=> 약 9%이상의 부정 승차를 방지 할 수 있을 것!

#### Program Flow

##### Android openAPI
- 공공데이터 포탈에서 서울특별시 버스위치정보 조회 / 노선정보조회 서비스 이용
1) Beacon에서 버스ID와 노선명 제공 - vehId, busRouteNm
2) getBusRouteList에서 busRouteNm을 조회하여 busRouteId를 얻음
3) getStationsByRouteList에서 busRouteId로 노선 리스트를 얻음
4) 노선을 화면에 Draw
5) getBusPosByVehIdItem에서 vehId를 이용해 버스의 실시간 위치 Draw

##### iBeacon Structure

##### Raspberry Pi 외부 구성

##### App과 User간의 Communication

##### Raspberry Pi와 App간의 Communication - TCP/IP 통신



Video URL : https://youtu.be/kGTkV1j1gr0
Paper PDF File : 
