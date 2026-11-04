-- 여행지·축제 사진 (D-063). scripts/fetch_commons_images.py 가 만든다 — 손으로 고치지 말 것.
-- image_path 가 빈 행만 채우므로 기동할 때마다 돌아도 되고, 이미 넣은 사진을 덮어쓰지 않는다.

UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6e/Odori_Park_%2815982613668%29.jpg/960px-Odori_Park_%2815982613668%29.jpg', t.image_credit = 'redlegsfan21 / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Odori_Park_(15982613668).jpg'
 WHERE p.name = '홋카이도' AND t.name = '삿포로 오도리 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/20200119_Sapporo_Clock_Tower.jpg/960px-20200119_Sapporo_Clock_Tower.jpg', t.image_credit = 'Balon Greyjoy / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:20200119_Sapporo_Clock_Tower.jpg'
 WHERE p.name = '홋카이도' AND t.name = '삿포로 시계탑' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Moiwa_Mountain_in_Sapporo_in_2009.jpg/960px-Moiwa_Mountain_in_Sapporo_in_2009.jpg', t.image_credit = 'Nikm / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Moiwa_Mountain_in_Sapporo_in_2009.jpg'
 WHERE p.name = '홋카이도' AND t.name = '모이와산 전망대' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bf/Otaru_Canal_on_July_19th%2C_2018.jpg/960px-Otaru_Canal_on_July_19th%2C_2018.jpg', t.image_credit = 'Choi2451 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Otaru_Canal_on_July_19th,_2018.jpg'
 WHERE p.name = '홋카이도' AND t.name = '오타루 운하' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Wide_view_of_Mount_Hakodate_from_Mashu-Maru.jpg/960px-Wide_view_of_Mount_Hakodate_from_Mashu-Maru.jpg', t.image_credit = '掬茶 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Wide_view_of_Mount_Hakodate_from_Mashu-Maru.jpg'
 WHERE p.name = '홋카이도' AND t.name = '하코다테산 야경' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/Hakodate_Asaichi_Hokkaido_Japan08s3.jpg/960px-Hakodate_Asaichi_Hokkaido_Japan08s3.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hakodate_Asaichi_Hokkaido_Japan08s3.jpg'
 WHERE p.name = '홋카이도' AND t.name = '하코다테 아침시장' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Hakodate_Goryokaku_Panorama_1.JPG/960px-Hakodate_Goryokaku_Panorama_1.JPG', t.image_credit = 'Keihin nike / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hakodate_Goryokaku_Panorama_1.JPG'
 WHERE p.name = '홋카이도' AND t.name = '고료카쿠 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/b/b6/Sign_of_Farm_Tomita.jpg', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sign_of_Farm_Tomita.jpg'
 WHERE p.name = '홋카이도' AND t.name = '후라노 팜 도미타' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/Biei_Blue-pond.jpg/960px-Biei_Blue-pond.jpg', t.image_credit = 'MaedaAkihiko / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Biei_Blue-pond.jpg'
 WHERE p.name = '홋카이도' AND t.name = '비에이 청의 호수' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/Asahiyama_zoo_entrance.jpg/960px-Asahiyama_zoo_entrance.jpg', t.image_credit = '欅 (Keyaki) / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Asahiyama_zoo_entrance.jpg'
 WHERE p.name = '홋카이도' AND t.name = '아사히야마 동물원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/65/Noboribetu_Jigokudani%EF%BC%8F%E7%99%BB%E5%88%A5%E5%9C%B0%E7%8D%84%E8%B0%B7%EF%BC%92_-_panoramio.jpg/960px-Noboribetu_Jigokudani%EF%BC%8F%E7%99%BB%E5%88%A5%E5%9C%B0%E7%8D%84%E8%B0%B7%EF%BC%92_-_panoramio.jpg', t.image_credit = 'T DMY / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Noboribetu_Jigokudani%EF%BC%8F%E7%99%BB%E5%88%A5%E5%9C%B0%E7%8D%84%E8%B0%B7%EF%BC%92_-_panoramio.jpg'
 WHERE p.name = '홋카이도' AND t.name = '노보리베쓰 지옥계곡' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6c/140829_Ichiko_of_Shiretoko_Goko_Lakes_Hokkaido_Japan01s5.jpg/960px-140829_Ichiko_of_Shiretoko_Goko_Lakes_Hokkaido_Japan01s5.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:140829_Ichiko_of_Shiretoko_Goko_Lakes_Hokkaido_Japan01s5.jpg'
 WHERE p.name = '홋카이도' AND t.name = '시레토코 오호' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/130922_Lake_Toya_Toyako_Hokkaido_Japan03s3.jpg/960px-130922_Lake_Toya_Toyako_Hokkaido_Japan03s3.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:130922_Lake_Toya_Toyako_Hokkaido_Japan03s3.jpg'
 WHERE p.name = '홋카이도' AND t.name = '도야호' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/54/Kushiro_marshland_of_winter_Hokkaido%2CJAPAN.jpg/960px-Kushiro_marshland_of_winter_Hokkaido%2CJAPAN.jpg', t.image_credit = 'Si-take. at Japanese Wikipedia This photo was taken with Olympus X-350 / D-575Z / C-360Z / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kushiro_marshland_of_winter_Hokkaido,JAPAN.jpg'
 WHERE p.name = '홋카이도' AND t.name = '구시로 습원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/Hirosaki_castle.jpg/960px-Hirosaki_castle.jpg', t.image_credit = 'Angaurits / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hirosaki_castle.jpg'
 WHERE p.name = '아오모리현' AND t.name = '히로사키 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/63/Oirase-keiryu.jpg/960px-Oirase-keiryu.jpg', t.image_credit = 'MaedaAkihiko / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Oirase-keiryu.jpg'
 WHERE p.name = '아오모리현' AND t.name = '오이라세 계류' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c0/Lake_Towada_from_Ohanabe_2008.jpg/960px-Lake_Towada_from_Ohanabe_2008.jpg', t.image_credit = 'Soica2001 (talk) / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Lake_Towada_from_Ohanabe_2008.jpg'
 WHERE p.name = '아오모리현' AND t.name = '도와다호' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/140913_Sannai-Maruyama_site_Aomori_Japan01bs6bs6.jpg/960px-140913_Sannai-Maruyama_site_Aomori_Japan01bs6bs6.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:140913_Sannai-Maruyama_site_Aomori_Japan01bs6bs6.jpg'
 WHERE p.name = '아오모리현' AND t.name = '산나이마루야마 유적' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9b/230728_Chusonji_Hiraizumi_Iwate_pref_Japan01s3.jpg/960px-230728_Chusonji_Hiraizumi_Iwate_pref_Japan01s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:230728_Chusonji_Hiraizumi_Iwate_pref_Japan01s3.jpg'
 WHERE p.name = '이와테현' AND t.name = '주손지 곤지키도' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8f/230729_Geibikei_Ichinoseki_Iwate_pref_Japan22s3.jpg/960px-230729_Geibikei_Ichinoseki_Iwate_pref_Japan22s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:230729_Geibikei_Ichinoseki_Iwate_pref_Japan22s3.jpg'
 WHERE p.name = '이와테현' AND t.name = '게이비계' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/9/93/Jyoudogahama.jpg', t.image_credit = 'Junpei Satoh / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Jyoudogahama.jpg'
 WHERE p.name = '이와테현' AND t.name = '조도가하마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/Matsushima_otakamori08Dec07.jpg/960px-Matsushima_otakamori08Dec07.jpg', t.image_credit = 'Kumamushi / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Matsushima_otakamori08Dec07.jpg'
 WHERE p.name = '미야기현' AND t.name = '마쓰시마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/f/ff/SendaiJoOtemonSumiYagura2003-11.jpg', t.image_credit = 'Kinori / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:SendaiJoOtemonSumiYagura2003-11.jpg'
 WHERE p.name = '미야기현' AND t.name = '센다이성 터' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/54/Zuiho-den06s3200.jpg/960px-Zuiho-den06s3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Zuiho-den06s3200.jpg'
 WHERE p.name = '미야기현' AND t.name = '즈이호덴' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d9/Zao_L.Okama.JPG/960px-Zao_L.Okama.JPG', t.image_credit = 'Qwert1234 / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Zao_L.Okama.JPG'
 WHERE p.name = '미야기현' AND t.name = '자오 오카마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/aa/%E8%A7%92%E9%A4%A8%E6%AD%A6%E5%AE%B6%E5%B1%8B%E6%95%B7%E9%80%9A%E3%82%8A20250118-P1063209.jpg/960px-%E8%A7%92%E9%A4%A8%E6%AD%A6%E5%AE%B6%E5%B1%8B%E6%95%B7%E9%80%9A%E3%82%8A20250118-P1063209.jpg', t.image_credit = 'くろふね / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E8%A7%92%E9%A4%A8%E6%AD%A6%E5%AE%B6%E5%B1%8B%E6%95%B7%E9%80%9A%E3%82%8A20250118-P1063209.jpg'
 WHERE p.name = '아키타현' AND t.name = '가쿠노다테 무가 저택 거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Pathway_to_the_Qkamura_Nyuto_Onsenkyo.jpg/960px-Pathway_to_the_Qkamura_Nyuto_Onsenkyo.jpg', t.image_credit = '掬茶 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Pathway_to_the_Qkamura_Nyuto_Onsenkyo.jpg'
 WHERE p.name = '아키타현' AND t.name = '뉴토 온천향' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/43/Lake_Tazawa_and_Kansa-g%C5%AB_20210213.jpg/960px-Lake_Tazawa_and_Kansa-g%C5%AB_20210213.jpg', t.image_credit = '(Tea-making tea) 掬茶 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Lake_Tazawa_and_Kansa-g%C5%AB_20210213.jpg'
 WHERE p.name = '아키타현' AND t.name = '다자와호' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3a/Risshaku-ji_konponchudo.jpg/960px-Risshaku-ji_konponchudo.jpg', t.image_credit = 'Cp9asngf / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Risshaku-ji_konponchudo.jpg'
 WHERE p.name = '야마가타현' AND t.name = '야마데라 릿샤쿠지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0b/Evening_twilight_in_Ginzan_Onsen_town_January_2022_A.jpg/960px-Evening_twilight_in_Ginzan_Onsen_town_January_2022_A.jpg', t.image_credit = 'さかおり / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Evening_twilight_in_Ginzan_Onsen_town_January_2022_A.jpg'
 WHERE p.name = '야마가타현' AND t.name = '긴잔 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/5/5c/Yamagata-zao_ski_1_%28200712%29.jpg', t.image_credit = 'E-190 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Yamagata-zao_ski_1_(200712).jpg'
 WHERE p.name = '야마가타현' AND t.name = '자오 온천 스키장' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/%E8%8B%A5%E6%9D%BE%E5%9F%8E%EF%BC%88%E9%B6%B4%E3%83%B6%E5%9F%8E%EF%BC%8920220917-P1003471.jpg/960px-%E8%8B%A5%E6%9D%BE%E5%9F%8E%EF%BC%88%E9%B6%B4%E3%83%B6%E5%9F%8E%EF%BC%8920220917-P1003471.jpg', t.image_credit = 'くろふね / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E8%8B%A5%E6%9D%BE%E5%9F%8E%EF%BC%88%E9%B6%B4%E3%83%B6%E5%9F%8E%EF%BC%8920220917-P1003471.jpg'
 WHERE p.name = '후쿠시마현' AND t.name = '쓰루가성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/6/61/Ja-fukushima-ohuchijuku-4.jpg', t.image_credit = 'ja:User:Fluffy nns. Uploaded by 百楽兎 / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ja-fukushima-ohuchijuku-4.jpg'
 WHERE p.name = '후쿠시마현' AND t.name = '오우치주쿠' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bf/Goshikinuma-azuma_0810.JPG/960px-Goshikinuma-azuma_0810.JPG', t.image_credit = 'Qwert1234 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Goshikinuma-azuma_0810.JPG'
 WHERE p.name = '후쿠시마현' AND t.name = '고시키누마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Ferris_wheel_of_the_Hitachi_beach_park%2Chitachi-kaihin-koen%2Chitachinaka-city%2Cjapan.JPG/960px-Ferris_wheel_of_the_Hitachi_beach_park%2Chitachi-kaihin-koen%2Chitachinaka-city%2Cjapan.JPG', t.image_credit = 'katorisi / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ferris_wheel_of_the_Hitachi_beach_park,hitachi-kaihin-koen,hitachinaka-city,japan.JPG'
 WHERE p.name = '이바라키현' AND t.name = '히타치 해변공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/1/14/Kairakuen_bairin.jpg', t.image_credit = '俊 at Japanese Wikipedia / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kairakuen_bairin.jpg'
 WHERE p.name = '이바라키현' AND t.name = '가이라쿠엔' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/da/Fukuroda_Falls_38.jpg/960px-Fukuroda_Falls_38.jpg', t.image_credit = 'Σ64 / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Fukuroda_Falls_38.jpg'
 WHERE p.name = '이바라키현' AND t.name = '후쿠로다 폭포' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b5/Nikko_Toshogu_Yomeimon_Gate_2024.jpg/960px-Nikko_Toshogu_Yomeimon_Gate_2024.jpg', t.image_credit = 'Jpatokal / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nikko_Toshogu_Yomeimon_Gate_2024.jpg'
 WHERE p.name = '도치기현' AND t.name = '닛코 도쇼구' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/Lake_chuzenji_and_kegon_waterfall.jpg/960px-Lake_chuzenji_and_kegon_waterfall.jpg', t.image_credit = 'Uraomote yamaneko / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Lake_chuzenji_and_kegon_waterfall.jpg'
 WHERE p.name = '도치기현' AND t.name = '게곤 폭포' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/Mount_nantai_and_lake_chuzenji.jpg/960px-Mount_nantai_and_lake_chuzenji.jpg', t.image_credit = 'Uraomote yamaneko / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mount_nantai_and_lake_chuzenji.jpg'
 WHERE p.name = '도치기현' AND t.name = '주젠지호' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/36/Japanese_wisteria%2C_Ashikaga_Flower_Park_13.jpg/960px-Japanese_wisteria%2C_Ashikaga_Flower_Park_13.jpg', t.image_credit = 'SLIMHANNYA / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Japanese_wisteria,_Ashikaga_Flower_Park_13.jpg'
 WHERE p.name = '도치기현' AND t.name = '아시카가 플라워파크' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/%E6%B9%AF%E7%95%912.jpg/960px-%E6%B9%AF%E7%95%912.jpg', t.image_credit = 'Mikkabie / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E6%B9%AF%E7%95%912.jpg'
 WHERE p.name = '군마현' AND t.name = '구사쓰 온천 유바타케' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/82/Ikaho_Onsen_03.jpg/960px-Ikaho_Onsen_03.jpg', t.image_credit = 'matsukaz / CC BY 2.1 jp / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ikaho_Onsen_03.jpg'
 WHERE p.name = '군마현' AND t.name = '이카호 온천 돌계단 거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2a/Tomioka_Silk_Mill_East_Cocoon_Warehouse05.jpg/960px-Tomioka_Silk_Mill_East_Cocoon_Warehouse05.jpg', t.image_credit = 'Hasec / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tomioka_Silk_Mill_East_Cocoon_Warehouse05.jpg'
 WHERE p.name = '군마현' AND t.name = '도미오카 제사장' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/74/Kurazukuri_Street_in_Little_Edo.jpg/960px-Kurazukuri_Street_in_Little_Edo.jpg', t.image_credit = 'Dandy1022 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kurazukuri_Street_in_Little_Edo.jpg'
 WHERE p.name = '사이타마현' AND t.name = '가와고에 구라즈쿠리 거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/35/2009-02-08_%E9%95%B7%E7%80%9E_Nagatoro_06.jpg/960px-2009-02-08_%E9%95%B7%E7%80%9E_Nagatoro_06.jpg', t.image_credit = 'ajari / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:2009-02-08_%E9%95%B7%E7%80%9E_Nagatoro_06.jpg'
 WHERE p.name = '사이타마현' AND t.name = '나가토로 라인 강 하류' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/f/f5/The_Railway_Museum_Park_Zone_20071014_%281%29.jpg', t.image_credit = 'Lover of Romance / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:The_Railway_Museum_Park_Zone_20071014_(1).jpg'
 WHERE p.name = '사이타마현' AND t.name = '철도박물관' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/1/15/Naritasan-Shinshoji-Temple-2008.jpg', t.image_credit = 'Hoku-sou-san / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Naritasan-Shinshoji-Temple-2008.jpg'
 WHERE p.name = '지바현' AND t.name = '나리타산 신쇼지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/91/A8_Tokyo_Disneyland.jpg/960px-A8_Tokyo_Disneyland.jpg', t.image_credit = 'Øyvind Holmstad / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:A8_Tokyo_Disneyland.jpg'
 WHERE p.name = '지바현' AND t.name = '도쿄 디즈니 리조트' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/3/35/The_port_of_Kanaya.jpg', t.image_credit = 'UE-PON2600 at Japanese Wikipedia / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:The_port_of_Kanaya.jpg'
 WHERE p.name = '지바현' AND t.name = '노코기리산' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e7/A_view_of_Sawara-Honmachi.JPG/960px-A_view_of_Sawara-Honmachi.JPG', t.image_credit = 'Tamaki / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:A_view_of_Sawara-Honmachi.JPG'
 WHERE p.name = '지바현' AND t.name = '사와라 거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/Sensoji_Temple_%2840909716384%29.jpg/960px-Sensoji_Temple_%2840909716384%29.jpg', t.image_credit = 'Ajay Suresh from New York, NY, USA / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sensoji_Temple_(40909716384).jpg'
 WHERE p.name = '도쿄도' AND t.name = '센소지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ed/Courtyard_of_Meiji_Shrine_20190717.jpg/960px-Courtyard_of_Meiji_Shrine_20190717.jpg', t.image_credit = 'Tokuzo in Edomura / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Courtyard_of_Meiji_Shrine_20190717.jpg'
 WHERE p.name = '도쿄도' AND t.name = '메이지 신궁' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c5/Tokyo_Skytree_%26_East_Tower.jpg/960px-Tokyo_Skytree_%26_East_Tower.jpg', t.image_credit = 'Kakidai / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tokyo_Skytree_%26_East_Tower.jpg'
 WHERE p.name = '도쿄도' AND t.name = '도쿄 스카이트리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1c/Tokyo_Tower_M4854.jpg/960px-Tokyo_Tower_M4854.jpg', t.image_credit = 'Fg2 / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tokyo_Tower_M4854.jpg'
 WHERE p.name = '도쿄도' AND t.name = '도쿄 타워' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c2/2018_Shibuya_Crossing.jpg/960px-2018_Shibuya_Crossing.jpg', t.image_credit = 'Kakidai / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:2018_Shibuya_Crossing.jpg'
 WHERE p.name = '도쿄도' AND t.name = '시부야 스크램블 교차로' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9d/Shinjuku_Gyoen_National_Garden_-_sakura_3.JPG/960px-Shinjuku_Gyoen_National_Garden_-_sakura_3.JPG', t.image_credit = 'Kakidai / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Shinjuku_Gyoen_National_Garden_-_sakura_3.JPG'
 WHERE p.name = '도쿄도' AND t.name = '신주쿠 교엔' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Sakura_in_Ueno_Park.jpg/960px-Sakura_in_Ueno_Park.jpg', t.image_credit = 'Volfgang (talk) / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sakura_in_Ueno_Park.jpg'
 WHERE p.name = '도쿄도' AND t.name = '우에노 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fd/2018_Tsukiji_fish_market.jpg/960px-2018_Tsukiji_fish_market.jpg', t.image_credit = 'Kakidai / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:2018_Tsukiji_fish_market.jpg'
 WHERE p.name = '도쿄도' AND t.name = '쓰키지 장외시장' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/75/Odaiba_from_Tokyo_Tower_Day.jpg/960px-Odaiba_from_Tokyo_Tower_Day.jpg', t.image_credit = 'Chris 73 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Odaiba_from_Tokyo_Tower_Day.jpg'
 WHERE p.name = '도쿄도' AND t.name = '오다이바' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/%E7%A7%8B%E8%91%89%E5%8E%9F%E4%B8%AD%E5%A4%AE%E9%80%9A%E3%82%8A%E4%BA%A4%E5%B7%AE%E7%82%B920200419.jpg/960px-%E7%A7%8B%E8%91%89%E5%8E%9F%E4%B8%AD%E5%A4%AE%E9%80%9A%E3%82%8A%E4%BA%A4%E5%B7%AE%E7%82%B920200419.jpg', t.image_credit = '片割れ靴下 (unpaired sock) / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E7%A7%8B%E8%91%89%E5%8E%9F%E4%B8%AD%E5%A4%AE%E9%80%9A%E3%82%8A%E4%BA%A4%E5%B7%AE%E7%82%B920200419.jpg'
 WHERE p.name = '도쿄도' AND t.name = '아키하바라 전자상가' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f2/%C5%8Cte-mon_gate%2C_Edo_Castle_Tokyo_Japan_by_Don_Ramey_Logan.jpg/960px-%C5%8Cte-mon_gate%2C_Edo_Castle_Tokyo_Japan_by_Don_Ramey_Logan.jpg', t.image_credit = 'Don Ramey Logan / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%C5%8Cte-mon_gate,_Edo_Castle_Tokyo_Japan_by_Don_Ramey_Logan.jpg'
 WHERE p.name = '도쿄도' AND t.name = '고쿄 히가시교엔' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/Mount_Takao_hike_in_January_9.jpg/960px-Mount_Takao_hike_in_January_9.jpg', t.image_credit = 'Syced / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mount_Takao_hike_in_January_9.jpg'
 WHERE p.name = '도쿄도' AND t.name = '다카오산' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/2/2c/Ghibli-museum-mitaka.jpg', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ghibli-museum-mitaka.jpg'
 WHERE p.name = '도쿄도' AND t.name = '지브리 미술관' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d3/Kamakura_Daibutsu-2.jpg/960px-Kamakura_Daibutsu-2.jpg', t.image_credit = '江戸村のとくぞう / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kamakura_Daibutsu-2.jpg'
 WHERE p.name = '가나가와현' AND t.name = '가마쿠라 대불' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/59/Tsurugaoka_Hachimangu_001.jpg/960px-Tsurugaoka_Hachimangu_001.jpg', t.image_credit = 'Ocdp / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tsurugaoka_Hachimangu_001.jpg'
 WHERE p.name = '가나가와현' AND t.name = '쓰루가오카 하치만구' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Enoshima_06.jpg/960px-Enoshima_06.jpg', t.image_credit = 'Σ64 / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Enoshima_06.jpg'
 WHERE p.name = '가나가와현' AND t.name = '에노시마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c2/161223_Owakudani_Station_Hakone_Japan02s3.jpg/960px-161223_Owakudani_Station_Hakone_Japan02s3.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:161223_Owakudani_Station_Hakone_Japan02s3.jpg'
 WHERE p.name = '가나가와현' AND t.name = '하코네 오와쿠다니' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/AshinokoYuransen8382.jpg/960px-AshinokoYuransen8382.jpg', t.image_credit = 'Ritsuka (talk · contribs) / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:AshinokoYuransen8382.jpg'
 WHERE p.name = '가나가와현' AND t.name = '아시노코' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4b/Yokohama_Chinatown_signage_2015.jpg/960px-Yokohama_Chinatown_signage_2015.jpg', t.image_credit = 'Wpcpey / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Yokohama_Chinatown_signage_2015.jpg'
 WHERE p.name = '가나가와현' AND t.name = '요코하마 차이나타운' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b8/Minatomirai_Station_Exit5.jpg/960px-Minatomirai_Station_Exit5.jpg', t.image_credit = 'RGB256 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Minatomirai_Station_Exit5.jpg'
 WHERE p.name = '가나가와현' AND t.name = '미나토미라이 21' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/89/SadoGaShima.jpg/960px-SadoGaShima.jpg', t.image_credit = 'BehBeh at Japanese Wikipedia / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:SadoGaShima.jpg'
 WHERE p.name = '니가타현' AND t.name = '사도섬' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/05/Entrance_to_Echigo-Yuzawa_Onsen.JPG/960px-Entrance_to_Echigo-Yuzawa_Onsen.JPG', t.image_credit = 'Abasaa / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Entrance_to_Echigo-Yuzawa_Onsen.JPG'
 WHERE p.name = '니가타현' AND t.name = '에치고 유자와 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/3/32/Kiyotsukyo_Summer.jpg', t.image_credit = '清兵衛 at Japanese Wikipedia / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kiyotsukyo_Summer.jpg'
 WHERE p.name = '니가타현' AND t.name = '기요쓰쿄 협곡 터널' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cb/Murodoh.jpg/960px-Murodoh.jpg', t.image_credit = 'Papakuro at Japanese Wikipedia / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Murodoh.jpg'
 WHERE p.name = '도야마현' AND t.name = '다테야마 구로베 알펜루트' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Kurobe_Dam_survey.jpg/960px-Kurobe_Dam_survey.jpg', t.image_credit = 'Qurren / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kurobe_Dam_survey.jpg'
 WHERE p.name = '도야마현' AND t.name = '구로베 댐' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/99/%E4%B8%96%E7%95%8C%E9%81%BA%E7%94%A3%E8%8F%85%E6%B2%BC%E5%90%88%E6%8E%8C%E9%9B%86%E8%90%BD.jpg/960px-%E4%B8%96%E7%95%8C%E9%81%BA%E7%94%A3%E8%8F%85%E6%B2%BC%E5%90%88%E6%8E%8C%E9%9B%86%E8%90%BD.jpg', t.image_credit = 'Solarisjapan / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E4%B8%96%E7%95%8C%E9%81%BA%E7%94%A3%E8%8F%85%E6%B2%BC%E5%90%88%E6%8E%8C%E9%9B%86%E8%90%BD.jpg'
 WHERE p.name = '도야마현' AND t.name = '고카야마 합장 마을' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/a/ac/Kenrokuen_bridge.jpg', t.image_credit = 'Chris Gladis from Kyoto, Japan / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kenrokuen_bridge.jpg'
 WHERE p.name = '이시카와현' AND t.name = '겐로쿠엔' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7e/Kanazawa-M-5932.jpg/960px-Kanazawa-M-5932.jpg', t.image_credit = 'Fg2 / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kanazawa-M-5932.jpg'
 WHERE p.name = '이시카와현' AND t.name = '가나자와성 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/%E3%81%B2%E3%81%8C%E3%81%97%E8%8C%B6%E5%B1%8B%E8%A1%97_Higashiyama%2C_Kanazawa_-_Jan_21%2C_2017.jpg/960px-%E3%81%B2%E3%81%8C%E3%81%97%E8%8C%B6%E5%B1%8B%E8%A1%97_Higashiyama%2C_Kanazawa_-_Jan_21%2C_2017.jpg', t.image_credit = '寅次郎 / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E3%81%B2%E3%81%8C%E3%81%97%E8%8C%B6%E5%B1%8B%E8%A1%97_Higashiyama,_Kanazawa_-_Jan_21,_2017.jpg'
 WHERE p.name = '이시카와현' AND t.name = '히가시 차야 거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/Omicho-ichiba_%E8%BF%91%E6%B1%9F%E7%94%BA%E5%B8%82%E5%A0%B4_A091185.JPG/960px-Omicho-ichiba_%E8%BF%91%E6%B1%9F%E7%94%BA%E5%B8%82%E5%A0%B4_A091185.JPG', t.image_credit = '松岡明芳 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Omicho-ichiba_%E8%BF%91%E6%B1%9F%E7%94%BA%E5%B8%82%E5%A0%B4_A091185.JPG'
 WHERE p.name = '이시카와현' AND t.name = '오미초 시장' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a3/Wakura_Onsen_Kagaya_200906-2.jpg/960px-Wakura_Onsen_Kagaya_200906-2.jpg', t.image_credit = 'Totti / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Wakura_Onsen_Kagaya_200906-2.jpg'
 WHERE p.name = '이시카와현' AND t.name = '와쿠라 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bd/Japan_Tojinbo02n4592.jpg/960px-Japan_Tojinbo02n4592.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Japan_Tojinbo02n4592.jpg'
 WHERE p.name = '후쿠이현' AND t.name = '도진보' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d1/Eihei-ji_Temple_Chokushimon%2C_Eiheiji-cho_2013.jpg/960px-Eihei-ji_Temple_Chokushimon%2C_Eiheiji-cho_2013.jpg', t.image_credit = 'Tomio344456 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Eihei-ji_Temple_Chokushimon,_Eiheiji-cho_2013.jpg'
 WHERE p.name = '후쿠이현' AND t.name = '에이헤이지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/13/Fukui_Prefectural_Dinosaur_Museum_20231217.jpg/960px-Fukui_Prefectural_Dinosaur_Museum_20231217.jpg', t.image_credit = 'Totti / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Fukui_Prefectural_Dinosaur_Museum_20231217.jpg'
 WHERE p.name = '후쿠이현' AND t.name = '후쿠이현립 공룡박물관' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/18/KawaguchiKo.jpg/960px-KawaguchiKo.jpg', t.image_credit = 'BehBeh / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:KawaguchiKo.jpg'
 WHERE p.name = '야마나시현' AND t.name = '가와구치코' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/66/Chuurei-tou_Fujiyoshida_17025277650_c59733d6ba_o.jpg/960px-Chuurei-tou_Fujiyoshida_17025277650_c59733d6ba_o.jpg', t.image_credit = 'https://www.flickr.com/photos/reggiepen/ / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Chuurei-tou_Fujiyoshida_17025277650_c59733d6ba_o.jpg'
 WHERE p.name = '야마나시현' AND t.name = '아라쿠라야마 센겐 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/231028_Waku-ike_Oshino_Hakkai_springs_Oshino_Yamanashi_pref_Japan01s3.jpg/960px-231028_Waku-ike_Oshino_Hakkai_springs_Oshino_Yamanashi_pref_Japan01s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:231028_Waku-ike_Oshino_Hakkai_springs_Oshino_Yamanashi_pref_Japan01s3.jpg'
 WHERE p.name = '야마나시현' AND t.name = '오시노 핫카이' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cc/Kakuenpou_in_autumn.jpg/960px-Kakuenpou_in_autumn.jpg', t.image_credit = 'Captain76 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kakuenpou_in_autumn.jpg'
 WHERE p.name = '야마나시현' AND t.name = '쇼센쿄' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/130608_Matsumoto_Castle_Matsumoto_Nagano_pref_Japan02bs4.jpg/960px-130608_Matsumoto_Castle_Matsumoto_Nagano_pref_Japan02bs4.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:130608_Matsumoto_Castle_Matsumoto_Nagano_pref_Japan02bs4.jpg'
 WHERE p.name = '나가노현' AND t.name = '마쓰모토성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/150920_Mt_Hotaka-dake_Kamikochi_Japan01s3.jpg/960px-150920_Mt_Hotaka-dake_Kamikochi_Japan01s3.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:150920_Mt_Hotaka-dake_Kamikochi_Japan01s3.jpg'
 WHERE p.name = '나가노현' AND t.name = '가미코치' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e1/Zenkoji_Temple_Hondo.jpg/960px-Zenkoji_Temple_Hondo.jpg', t.image_credit = '快速踊り子 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Zenkoji_Temple_Hondo.jpg'
 WHERE p.name = '나가노현' AND t.name = '젠코지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/8/8c/Jigokudani_hotspring_in_Nagano_Japan_001.jpg', t.image_credit = 'Yosemite / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Jigokudani_hotspring_in_Nagano_Japan_001.jpg'
 WHERE p.name = '나가노현' AND t.name = '지고쿠다니 야생 원숭이 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ce/260829_Kyu-Karuizawa_Karuizawa_Japan02s3.jpg/960px-260829_Kyu-Karuizawa_Karuizawa_Japan02s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:260829_Kyu-Karuizawa_Karuizawa_Japan02s3.jpg'
 WHERE p.name = '나가노현' AND t.name = '가루이자와' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/7/78/Kisokaido42_Tsumago.jpg', t.image_credit = 'Utagawa Hiroshige / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kisokaido42_Tsumago.jpg'
 WHERE p.name = '나가노현' AND t.name = '쓰마고주쿠' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/02/Ogi_Shirakawa01n3200.jpg/960px-Ogi_Shirakawa01n3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ogi_Shirakawa01n3200.jpg'
 WHERE p.name = '기후현' AND t.name = '시라카와고' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/91/Kamisannomachi%2C_Takayama_City_2015.jpg/960px-Kamisannomachi%2C_Takayama_City_2015.jpg', t.image_credit = 'Tomio344456 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kamisannomachi,_Takayama_City_2015.jpg'
 WHERE p.name = '기후현' AND t.name = '다카야마 옛 거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1b/260125_Gero_Onsen_Gero_Gifu_pref_Japan01s3.jpg/960px-260125_Gero_Onsen_Gero_Gifu_pref_Japan01s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:260125_Gero_Onsen_Gero_Gifu_pref_Japan01s3.jpg'
 WHERE p.name = '기후현' AND t.name = '게로 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7a/%E6%96%B0%E7%A9%82%E9%AB%98%E3%83%AD%E3%83%BC%E3%83%97%E3%82%A6%E3%82%A7%E3%82%A4%E5%B1%95%E6%9C%9B%E5%8F%B0_-_panoramio.jpg/960px-%E6%96%B0%E7%A9%82%E9%AB%98%E3%83%AD%E3%83%BC%E3%83%97%E3%82%A6%E3%82%A7%E3%82%A4%E5%B1%95%E6%9C%9B%E5%8F%B0_-_panoramio.jpg', t.image_credit = 'alonfloc / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E6%96%B0%E7%A9%82%E9%AB%98%E3%83%AD%E3%83%BC%E3%83%97%E3%82%A6%E3%82%A7%E3%82%A4%E5%B1%95%E6%9C%9B%E5%8F%B0_-_panoramio.jpg'
 WHERE p.name = '기후현' AND t.name = '신호타카 로프웨이' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Mihojapan.jpg/960px-Mihojapan.jpg', t.image_credit = 'Hiroshige / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mihojapan.jpg'
 WHERE p.name = '시즈오카현' AND t.name = '미호노마쓰바라' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/06/Atami_20120915_c.jpg/960px-Atami_20120915_c.jpg', t.image_credit = 'Batholith / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Atami_20120915_c.jpg'
 WHERE p.name = '시즈오카현' AND t.name = '아타미 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6d/181124_Shuzenji_Izu_Shizuoka_pref_Japan04s3.jpg/960px-181124_Shuzenji_Izu_Shizuoka_pref_Japan04s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:181124_Shuzenji_Izu_Shizuoka_pref_Japan04s3.jpg'
 WHERE p.name = '시즈오카현' AND t.name = '슈젠지 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/85/%E4%B9%85%E8%83%BD%E5%B1%B1%E6%9D%B1%E7%85%A7%E5%AE%AE%E6%A5%BC%E9%96%80.png/960px-%E4%B9%85%E8%83%BD%E5%B1%B1%E6%9D%B1%E7%85%A7%E5%AE%AE%E6%A5%BC%E9%96%80.png', t.image_credit = 'SHIZUOKACITYperson / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E4%B9%85%E8%83%BD%E5%B1%B1%E6%9D%B1%E7%85%A7%E5%AE%AE%E6%A5%BC%E9%96%80.png'
 WHERE p.name = '시즈오카현' AND t.name = '구노잔 도쇼구' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/56/Nagoya_Castle_7.jpg/960px-Nagoya_Castle_7.jpg', t.image_credit = 'Bariston / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nagoya_Castle_7.jpg'
 WHERE p.name = '아이치현' AND t.name = '나고야성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cb/Atsuta-jinguu_shaden.JPG/960px-Atsuta-jinguu_shaden.JPG', t.image_credit = 'Saigen Jiro / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Atsuta-jinguu_shaden.JPG'
 WHERE p.name = '아이치현' AND t.name = '아쓰타 신궁' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/be/Expo_2005_of_Satsuki_and_Mei%E2%80%99s_House_02.jpg/960px-Expo_2005_of_Satsuki_and_Mei%E2%80%99s_House_02.jpg', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Expo_2005_of_Satsuki_and_Mei%E2%80%99s_House_02.jpg'
 WHERE p.name = '아이치현' AND t.name = '지브리 파크' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/Castle_in_Inuyama.JPG/960px-Castle_in_Inuyama.JPG', t.image_credit = 'ThorstenS / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Castle_in_Inuyama.JPG'
 WHERE p.name = '아이치현' AND t.name = '이누야마성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/5/58/%E5%A4%A7%E9%A0%88%E5%95%86%E5%BA%97%E8%A1%97_-_3467815545.jpg', t.image_credit = '0* PrincessCCCC / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%A4%A7%E9%A0%88%E5%95%86%E5%BA%97%E8%A1%97_-_3467815545.jpg'
 WHERE p.name = '아이치현' AND t.name = '오스 상점가' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Naiku_01.JPG/960px-Naiku_01.JPG', t.image_credit = 'N yotarou / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Naiku_01.JPG'
 WHERE p.name = '미에현' AND t.name = '이세 신궁' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Okage_Yokocho01.JPG/960px-Okage_Yokocho01.JPG', t.image_credit = 'Oilstreet / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Okage_Yokocho01.JPG'
 WHERE p.name = '미에현' AND t.name = '오카게 요코초' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a3/Futamiokitama_jinja_Haiden.jpg/960px-Futamiokitama_jinja_Haiden.jpg', t.image_credit = 'Yanajin33 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Futamiokitama_jinja_Haiden.jpg'
 WHERE p.name = '미에현' AND t.name = '메오토이와' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/70/Nagashima_spa_land_skyview.jpg/960px-Nagashima_spa_land_skyview.jpg', t.image_credit = 'Hideyuki KAMON from Takarazuka / 宝塚市, Hyogo / 兵庫県, Japan / 日本 / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nagashima_spa_land_skyview.jpg'
 WHERE p.name = '미에현' AND t.name = '나가시마 스파랜드' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Lake_biwa.jpg/960px-Lake_biwa.jpg', t.image_credit = 'Global Land Cover Facility (GLCF) / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Lake_biwa.jpg'
 WHERE p.name = '시가현' AND t.name = '비와호' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ab/Hikone_castle5537.JPG/960px-Hikone_castle5537.JPG', t.image_credit = '日:Jnn / CC BY 2.1 jp / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hikone_castle5537.JPG'
 WHERE p.name = '시가현' AND t.name = '히코네성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fd/Enryakuji_Konponchudo04n4272.jpg/960px-Enryakuji_Konponchudo04n4272.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Enryakuji_Konponchudo04n4272.jpg'
 WHERE p.name = '시가현' AND t.name = '엔랴쿠지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e7/Shirahige-jinja_%28Takashima%29_keidai.JPG/960px-Shirahige-jinja_%28Takashima%29_keidai.JPG', t.image_credit = 'Saigen Jiro This photo was taken with Nikon D3200 / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Shirahige-jinja_(Takashima)_keidai.JPG'
 WHERE p.name = '시가현' AND t.name = '시라히게 신사' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/64/Fushimiinari-taisha%2C_gehaiden-1.jpg/960px-Fushimiinari-taisha%2C_gehaiden-1.jpg', t.image_credit = 'Saigen Jiro / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Fushimiinari-taisha,_gehaiden-1.jpg'
 WHERE p.name = '교토부' AND t.name = '후시미 이나리 타이샤' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6d/Kyoto-Kiyomizudera%2C_Japan.jpg/960px-Kyoto-Kiyomizudera%2C_Japan.jpg', t.image_credit = 'Alberto Carrasco Casado / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kyoto-Kiyomizudera,_Japan.jpg'
 WHERE p.name = '교토부' AND t.name = '기요미즈데라' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/de/Water_reflection_of_Kinkaku-ji_Temple_a_sunny_day%2C_Kyoto%2C_Japan.jpg/960px-Water_reflection_of_Kinkaku-ji_Temple_a_sunny_day%2C_Kyoto%2C_Japan.jpg', t.image_credit = 'Basile Morin / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Water_reflection_of_Kinkaku-ji_Temple_a_sunny_day,_Kyoto,_Japan.jpg'
 WHERE p.name = '교토부' AND t.name = '킨카쿠지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Silver_Pavilion_%28Ginkaku%29_20260921.jpg/960px-Silver_Pavilion_%28Ginkaku%29_20260921.jpg', t.image_credit = '掬茶 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Silver_Pavilion_(Ginkaku)_20260921.jpg'
 WHERE p.name = '교토부' AND t.name = '긴카쿠지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/f/fb/%E5%B5%90%E5%B1%B1%E3%81%AE%E7%AB%B9%E6%9E%97%E3%81%AE%E5%B0%8F%E5%BE%84_%28The_bamboo_grove_in_Arashiyama%29_10_Jul%2C_2010_-_panoramio.jpg', t.image_credit = 'Hiroaki Kaneko / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%B5%90%E5%B1%B1%E3%81%AE%E7%AB%B9%E6%9E%97%E3%81%AE%E5%B0%8F%E5%BE%84_(The_bamboo_grove_in_Arashiyama)_10_Jul,_2010_-_panoramio.jpg'
 WHERE p.name = '교토부' AND t.name = '아라시야마 대나무 숲' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Togetsukyo_in_Kyoto_Arashiyama.jpg/960px-Togetsukyo_in_Kyoto_Arashiyama.jpg', t.image_credit = 'Blue Lotus from Arashiyama, Kyoto / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Togetsukyo_in_Kyoto_Arashiyama.jpg'
 WHERE p.name = '교토부' AND t.name = '도게쓰교' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/Nijo_Castle_1.jpg/960px-Nijo_Castle_1.jpg', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nijo_Castle_1.jpg'
 WHERE p.name = '교토부' AND t.name = '니조성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/65/Gion_Hanamik%C3%B4ji-d%C3%B4ri.jpg/960px-Gion_Hanamik%C3%B4ji-d%C3%B4ri.jpg', t.image_credit = 'Yanajin33 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Gion_Hanamik%C3%B4ji-d%C3%B4ri.jpg'
 WHERE p.name = '교토부' AND t.name = '기온 하나미코지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1f/150207_Ryoan-ji_Kyoto_Japan01s3.jpg/960px-150207_Ryoan-ji_Kyoto_Japan01s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:150207_Ryoan-ji_Kyoto_Japan01s3.jpg'
 WHERE p.name = '교토부' AND t.name = '료안지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/7/7b/Tetsugakunomichi02.jpg', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tetsugakunomichi02.jpg'
 WHERE p.name = '교토부' AND t.name = '철학의 길' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/A166_Japan_Kyoto_Customer_in_the_Nishiki_Galery_Market_%284764437764%29.jpg/960px-A166_Japan_Kyoto_Customer_in_the_Nishiki_Galery_Market_%284764437764%29.jpg', t.image_credit = 'DANIEL JULIE from Paris, France / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:A166_Japan_Kyoto_Customer_in_the_Nishiki_Galery_Market_(4764437764).jpg'
 WHERE p.name = '교토부' AND t.name = '니시키 시장' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/2/2a/Amanohashidate_from_south.jpg', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Amanohashidate_from_south.jpg'
 WHERE p.name = '교토부' AND t.name = '아마노하시다테' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e4/Osaka_Castle_02bs3200.jpg/960px-Osaka_Castle_02bs3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Osaka_Castle_02bs3200.jpg'
 WHERE p.name = '오사카부' AND t.name = '오사카성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/Dotombori_neon_signs.JPG/960px-Dotombori_neon_signs.JPG', t.image_credit = 'Tokumeigakarinoaoshima / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Dotombori_neon_signs.JPG'
 WHERE p.name = '오사카부' AND t.name = '도톤보리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/USJ_2018.09-24.jpg/960px-USJ_2018.09-24.jpg', t.image_credit = 'Rebirth10 / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:USJ_2018.09-24.jpg'
 WHERE p.name = '오사카부' AND t.name = '유니버설 스튜디오 재팬' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/95/Shinsekai_and_Tsutenkaku_at_night_2019-04-12.jpg/960px-Shinsekai_and_Tsutenkaku_at_night_2019-04-12.jpg', t.image_credit = 'Dick Thomas Johnson from Tokyo, Japan / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Shinsekai_and_Tsutenkaku_at_night_2019-04-12.jpg'
 WHERE p.name = '오사카부' AND t.name = '신세카이 쓰텐카쿠' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f4/Kuromon-ichiba_in_201408.JPG/960px-Kuromon-ichiba_in_201408.JPG', t.image_credit = 'Mc681 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kuromon-ichiba_in_201408.JPG'
 WHERE p.name = '오사카부' AND t.name = '구로몬 시장' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/43/Umeda_Sky_Building_2026.jpg/960px-Umeda_Sky_Building_2026.jpg', t.image_credit = 'Sakai Yayoi / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Umeda_Sky_Building_2026.jpg'
 WHERE p.name = '오사카부' AND t.name = '우메다 스카이 빌딩' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f4/Kaiy%C5%ABkan.jpg/960px-Kaiy%C5%ABkan.jpg', t.image_credit = 'Sakai Yayoi / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kaiy%C5%ABkan.jpg'
 WHERE p.name = '오사카부' AND t.name = '가이유칸' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fd/Sumiyoshi-taisha%2C_keidai-2.jpg/960px-Sumiyoshi-taisha%2C_keidai-2.jpg', t.image_credit = 'Saigen Jiro / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sumiyoshi-taisha,_keidai-2.jpg'
 WHERE p.name = '오사카부' AND t.name = '스미요시 타이샤' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Ch%C3%A2teau_de_Himeji02.jpg/960px-Ch%C3%A2teau_de_Himeji02.jpg', t.image_credit = 'Bernard Gagnon / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ch%C3%A2teau_de_Himeji02.jpg'
 WHERE p.name = '효고현' AND t.name = '히메지성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/%E7%A5%9E%E6%88%B8%E5%8C%97%E9%87%8E%E7%95%B0%E4%BA%BA%E9%A4%A8%E8%A1%97_-_panoramio.jpg/960px-%E7%A5%9E%E6%88%B8%E5%8C%97%E9%87%8E%E7%95%B0%E4%BA%BA%E9%A4%A8%E8%A1%97_-_panoramio.jpg', t.image_credit = '珈琲牛乳 / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E7%A5%9E%E6%88%B8%E5%8C%97%E9%87%8E%E7%95%B0%E4%BA%BA%E9%A4%A8%E8%A1%97_-_panoramio.jpg'
 WHERE p.name = '효고현' AND t.name = '고베 기타노 이진칸' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Kobe_harborland08s3200.jpg/960px-Kobe_harborland08s3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kobe_harborland08s3200.jpg'
 WHERE p.name = '효고현' AND t.name = '고베 하버랜드' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2c/170811_Rokko-Arima_Ropeway_Kobe_Japan00n.jpg/960px-170811_Rokko-Arima_Ropeway_Kobe_Japan00n.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:170811_Rokko-Arima_Ropeway_Kobe_Japan00n.jpg'
 WHERE p.name = '효고현' AND t.name = '아리마 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/65/Kinosaki_onsen02_1920.jpg/960px-Kinosaki_onsen02_1920.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kinosaki_onsen02_1920.jpg'
 WHERE p.name = '효고현' AND t.name = '기노사키 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/26/%E7%AB%B9%E7%94%B0%E5%9F%8E.JPG/960px-%E7%AB%B9%E7%94%B0%E5%9F%8E.JPG', t.image_credit = 'Wae35244 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E7%AB%B9%E7%94%B0%E5%9F%8E.JPG'
 WHERE p.name = '효고현' AND t.name = '다케다 성터' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/Todaiji18s3200.jpg/960px-Todaiji18s3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Todaiji18s3200.jpg'
 WHERE p.name = '나라현' AND t.name = '도다이지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/Nara_Park_-_panoramio_%282%29.jpg/960px-Nara_Park_-_panoramio_%282%29.jpg', t.image_credit = 'Feri88 / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nara_Park_-_panoramio_(2).jpg'
 WHERE p.name = '나라현' AND t.name = '나라 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0d/Kasuga-taisha11bs3200.jpg/960px-Kasuga-taisha11bs3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kasuga-taisha11bs3200.jpg'
 WHERE p.name = '나라현' AND t.name = '가스가 타이샤' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/Horyu-ji11s3200.jpg/960px-Horyu-ji11s3200.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Horyu-ji11s3200.jpg'
 WHERE p.name = '나라현' AND t.name = '호류지' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/79/Cherry_blossoms_at_Yoshinoyama_01.jpg/960px-Cherry_blossoms_at_Yoshinoyama_01.jpg', t.image_credit = 'Tawashi2006 / CC BY 2.1 jp / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Cherry_blossoms_at_Yoshinoyama_01.jpg'
 WHERE p.name = '나라현' AND t.name = '요시노산' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/Danjogaran_Koyasan12n3200.jpg/960px-Danjogaran_Koyasan12n3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Danjogaran_Koyasan12n3200.jpg'
 WHERE p.name = '와카야마현' AND t.name = '고야산' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/9/9c/Shrine_Kumano_nachi01.jpg', t.image_credit = 'Miya.m at Japanese Wikipedia / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Shrine_Kumano_nachi01.jpg'
 WHERE p.name = '와카야마현' AND t.name = '구마노 나치 타이샤' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d0/131221_Shirarahama_Beach_Shirahama_Wakayama_pref_Japan07s3bs.jpg/960px-131221_Shirarahama_Beach_Shirahama_Wakayama_pref_Japan07s3bs.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:131221_Shirarahama_Beach_Shirahama_Wakayama_pref_Japan07s3bs.jpg'
 WHERE p.name = '와카야마현' AND t.name = '시라하마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/54/Adventure_World%2C_Shirahama%2C_Wakayama_-_Apr_24%2C_2015.jpg/960px-Adventure_World%2C_Shirahama%2C_Wakayama_-_Apr_24%2C_2015.jpg', t.image_credit = 'Hideto KOBAYASHI / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Adventure_World,_Shirahama,_Wakayama_-_Apr_24,_2015.jpg'
 WHERE p.name = '와카야마현' AND t.name = '어드벤처 월드' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e1/Tottori-Sakyu_Tottori_Japan.JPG/960px-Tottori-Sakyu_Tottori_Japan.JPG', t.image_credit = 'Hashi photo / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tottori-Sakyu_Tottori_Japan.JPG'
 WHERE p.name = '돗토리현' AND t.name = '돗토리 사구' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/Sand_Museum.JPG/960px-Sand_Museum.JPG', t.image_credit = 'KASEI / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sand_Museum.JPG'
 WHERE p.name = '돗토리현' AND t.name = '모래 미술관' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/90/%E5%A2%83%E6%B8%AF%E6%9C%AC%E7%94%BA%E3%82%A2%E3%83%BC%E3%82%B1%E3%83%BC%E3%83%89%E5%95%86%E5%BA%97%E8%A1%972.JPG/960px-%E5%A2%83%E6%B8%AF%E6%9C%AC%E7%94%BA%E3%82%A2%E3%83%BC%E3%82%B1%E3%83%BC%E3%83%89%E5%95%86%E5%BA%97%E8%A1%972.JPG', t.image_credit = 'おぉたむすねィく探検隊 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%A2%83%E6%B8%AF%E6%9C%AC%E7%94%BA%E3%82%A2%E3%83%BC%E3%82%B1%E3%83%BC%E3%83%89%E5%95%86%E5%BA%97%E8%A1%972.JPG'
 WHERE p.name = '돗토리현' AND t.name = '미즈키 시게루 로드' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1c/Haiden_of_Izumo-taisha-1.JPG/960px-Haiden_of_Izumo-taisha-1.JPG', t.image_credit = 'Saigen Jiro / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Haiden_of_Izumo-taisha-1.JPG'
 WHERE p.name = '시마네현' AND t.name = '이즈모 타이샤' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Matsue_Castle_Tenshu.jpg/960px-Matsue_Castle_Tenshu.jpg', t.image_credit = 'by Monado / CC BY-SA 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Matsue_Castle_Tenshu.jpg'
 WHERE p.name = '시마네현' AND t.name = '마쓰에성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c8/Iwami_Ginzan_Silver_Mine%2C_Ryugenji_Mabu_Mine_Shaft_001.JPG/960px-Iwami_Ginzan_Silver_Mine%2C_Ryugenji_Mabu_Mine_Shaft_001.JPG', t.image_credit = 'Yama 1009 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Iwami_Ginzan_Silver_Mine,_Ryugenji_Mabu_Mine_Shaft_001.JPG'
 WHERE p.name = '시마네현' AND t.name = '이와미 은광 유적' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/64/Adachi_Museum_of_Art10st3200.jpg/960px-Adachi_Museum_of_Art10st3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Adachi_Museum_of_Art10st3200.jpg'
 WHERE p.name = '시마네현' AND t.name = '아다치 미술관' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Korakuen_09.JPG/960px-Korakuen_09.JPG', t.image_credit = 'by Reggaeman / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Korakuen_09.JPG'
 WHERE p.name = '오카야마현' AND t.name = '고라쿠엔' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/161015_At_Kurashiki_Bikan_historical_quarter_Kurashiki_Okayama_pref_Japan02s3.jpg/960px-161015_At_Kurashiki_Bikan_historical_quarter_Kurashiki_Okayama_pref_Japan02s3.jpg', t.image_credit = '663highland / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:161015_At_Kurashiki_Bikan_historical_quarter_Kurashiki_Okayama_pref_Japan02s3.jpg'
 WHERE p.name = '오카야마현' AND t.name = '구라시키 미관지구' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/16/Okayama_Castle_03.jpg/960px-Okayama_Castle_03.jpg', t.image_credit = 'by Reggaeman / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Okayama_Castle_03.jpg'
 WHERE p.name = '오카야마현' AND t.name = '오카야마성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/201201-TaroTokyo-Itsukushima-DSC08992.jpg/960px-201201-TaroTokyo-Itsukushima-DSC08992.jpg', t.image_credit = '東京太郎 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:201201-TaroTokyo-Itsukushima-DSC08992.jpg'
 WHERE p.name = '히로시마현' AND t.name = '이쓰쿠시마 신사' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7e/HiroshimaCenotaph_2008_01.JPG/960px-HiroshimaCenotaph_2008_01.JPG', t.image_credit = 'Taisyo / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:HiroshimaCenotaph_2008_01.JPG'
 WHERE p.name = '히로시마현' AND t.name = '평화 기념 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8e/Senkoji_Onomichi16s3872.jpg/960px-Senkoji_Onomichi16s3872.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Senkoji_Onomichi16s3872.jpg'
 WHERE p.name = '히로시마현' AND t.name = '오노미치' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/Okunojima-rabbit.JPG/960px-Okunojima-rabbit.JPG', t.image_credit = 'NY066 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Okunojima-rabbit.JPG'
 WHERE p.name = '히로시마현' AND t.name = '오쿠노시마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a6/Iwakuni%2C_ponte_kintai-kyo%2C_05.jpg/960px-Iwakuni%2C_ponte_kintai-kyo%2C_05.jpg', t.image_credit = 'Sailko / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Iwakuni,_ponte_kintai-kyo,_05.jpg'
 WHERE p.name = '야마구치현' AND t.name = '긴타이쿄' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/9/9b/%E5%85%83%E4%B9%83%E9%9A%85%E7%A8%B2%E6%88%90%E7%A5%9E%E7%A4%BE_Motonosumi-Inari-jinja_20090504.jpg', t.image_credit = 'noppo3 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%85%83%E4%B9%83%E9%9A%85%E7%A8%B2%E6%88%90%E7%A5%9E%E7%A4%BE_Motonosumi-Inari-jinja_20090504.jpg'
 WHERE p.name = '야마구치현' AND t.name = '모토노스미 신사' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/e/e8/Akiyoshi_Speleothem.jpg', t.image_credit = 'pastaitaken / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Akiyoshi_Speleothem.jpg'
 WHERE p.name = '야마구치현' AND t.name = '아키요시도' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6f/Naruto_Whirlpools_taken_4-21-2008.jpg/960px-Naruto_Whirlpools_taken_4-21-2008.jpg', t.image_credit = 'Hellbuny / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Naruto_Whirlpools_taken_4-21-2008.jpg'
 WHERE p.name = '도쿠시마현' AND t.name = '나루토 소용돌이' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/89/Iya_Valley_a.jpeg/960px-Iya_Valley_a.jpeg', t.image_credit = 'Yamato / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Iya_Valley_a.jpeg'
 WHERE p.name = '도쿠시마현' AND t.name = '이야 덩굴다리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/14/%C5%8Cboke_Valleys_001.jpg/960px-%C5%8Cboke_Valleys_001.jpg', t.image_credit = 'Naokijp / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%C5%8Cboke_Valleys_001.jpg'
 WHERE p.name = '도쿠시마현' AND t.name = '오보케 협곡' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3d/Kotohira-gu01s.jpg/960px-Kotohira-gu01s.jpg', t.image_credit = 'Toto-tarou / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kotohira-gu01s.jpg'
 WHERE p.name = '가가와현' AND t.name = '곤피라궁' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0b/Ritsurin.JPG/960px-Ritsurin.JPG', t.image_credit = 'Leela Soden / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ritsurin.JPG'
 WHERE p.name = '가가와현' AND t.name = '리쓰린 공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/4/42/Naoshima_islands.jpg', t.image_credit = 'Bakkai / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Naoshima_islands.jpg'
 WHERE p.name = '가가와현' AND t.name = '나오시마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/88/Angel_Road.jpg/960px-Angel_Road.jpg', t.image_credit = 'おにく / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Angel_Road.jpg'
 WHERE p.name = '가가와현' AND t.name = '쇼도시마 엔젤로드' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/Dogo_Hot_Spring2%28Matsuyama_City%29.JPG/960px-Dogo_Hot_Spring2%28Matsuyama_City%29.JPG', t.image_credit = 'Jyo81 (ja:User) / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Dogo_Hot_Spring2(Matsuyama_City).JPG'
 WHERE p.name = '에히메현' AND t.name = '도고 온천 본관' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Matsuyama_Castle_Tower_2_%28Iyo%29_JAPAN.JPG/960px-Matsuyama_Castle_Tower_2_%28Iyo%29_JAPAN.JPG', t.image_credit = 'Jyo81 (ja:User) / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Matsuyama_Castle_Tower_2_(Iyo)_JAPAN.JPG'
 WHERE p.name = '에히메현' AND t.name = '마쓰야마성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/%E8%A5%BF%E7%80%AC%E6%88%B8%E8%87%AA%E5%8B%95%E8%BB%8A%E9%81%93%28%E5%A4%A7%E6%B5%9CPA%29.jpg/960px-%E8%A5%BF%E7%80%AC%E6%88%B8%E8%87%AA%E5%8B%95%E8%BB%8A%E9%81%93%28%E5%A4%A7%E6%B5%9CPA%29.jpg', t.image_credit = 'Drivephotographer / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E8%A5%BF%E7%80%AC%E6%88%B8%E8%87%AA%E5%8B%95%E8%BB%8A%E9%81%93(%E5%A4%A7%E6%B5%9CPA).jpg'
 WHERE p.name = '에히메현' AND t.name = '시마나미 해도' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/4/4b/Kochi_Castle.jpg', t.image_credit = 'by Reggaeman / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kochi_Castle.jpg'
 WHERE p.name = '고치현' AND t.name = '고치성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/79/Kochi_Katsurahama_Daytime_2.JPG/960px-Kochi_Katsurahama_Daytime_2.JPG', t.image_credit = '京浜にけ / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kochi_Katsurahama_Daytime_2.JPG'
 WHERE p.name = '고치현' AND t.name = '가쓰라하마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cc/Shimantogawa_2000-08.jpeg/960px-Shimantogawa_2000-08.jpeg', t.image_credit = 'Hokanomono / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Shimantogawa_2000-08.jpeg'
 WHERE p.name = '고치현' AND t.name = '시만토강' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/Dazaihu_keinai.jpg/960px-Dazaihu_keinai.jpg', t.image_credit = '上条ジョー / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Dazaihu_keinai.jpg'
 WHERE p.name = '후쿠오카현' AND t.name = '다자이후 텐만구' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/20/Nishi-nakasu_btween_Naka_River_and_Yakuin-shinkawa_River_Ch%C5%AB%C5%8D-ku_Fukuoka_City_20221124.jpg/960px-Nishi-nakasu_btween_Naka_River_and_Yakuin-shinkawa_River_Ch%C5%AB%C5%8D-ku_Fukuoka_City_20221124.jpg', t.image_credit = 'Hirho / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nishi-nakasu_btween_Naka_River_and_Yakuin-shinkawa_River_Ch%C5%AB%C5%8D-ku_Fukuoka_City_20221124.jpg'
 WHERE p.name = '후쿠오카현' AND t.name = '나카스 포장마차 거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4b/Kushidajinjafukuoka01.jpg/960px-Kushidajinjafukuoka01.jpg', t.image_credit = 'Pontafon / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kushidajinjafukuoka01.jpg'
 WHERE p.name = '후쿠오카현' AND t.name = '구시다 신사' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Fukuoka_Tower.JPG/960px-Fukuoka_Tower.JPG', t.image_credit = 'Vickerman625 at English Wikipedia / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Fukuoka_Tower.JPG'
 WHERE p.name = '후쿠오카현' AND t.name = '후쿠오카 타워' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a6/%E6%9F%B3%E5%B7%9D%E6%B0%91%E6%AC%8A%E8%B7%AF%E6%AE%B5.jpg/960px-%E6%9F%B3%E5%B7%9D%E6%B0%91%E6%AC%8A%E8%B7%AF%E6%AE%B5.jpg', t.image_credit = 'Fcuk1203 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E6%9F%B3%E5%B7%9D%E6%B0%91%E6%AC%8A%E8%B7%AF%E6%AE%B5.jpg'
 WHERE p.name = '후쿠오카현' AND t.name = '야나가와 뱃놀이' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/140721_Mojiko_Retro_Kitakyushu_Japan01s3.jpg/960px-140721_Mojiko_Retro_Kitakyushu_Japan01s3.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:140721_Mojiko_Retro_Kitakyushu_Japan01s3.jpg'
 WHERE p.name = '후쿠오카현' AND t.name = '모지코 레트로' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/49/%E5%90%89%E9%87%8E%E3%83%B6%E9%87%8C%E6%AD%B4%E5%8F%B2%E5%85%AC%E5%9C%92_-_panoramio.jpg/960px-%E5%90%89%E9%87%8E%E3%83%B6%E9%87%8C%E6%AD%B4%E5%8F%B2%E5%85%AC%E5%9C%92_-_panoramio.jpg', t.image_credit = 'shikabane taro / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%90%89%E9%87%8E%E3%83%B6%E9%87%8C%E6%AD%B4%E5%8F%B2%E5%85%AC%E5%9C%92_-_panoramio.jpg'
 WHERE p.name = '사가현' AND t.name = '요시노가리 역사공원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/b/b0/ImariA.JPG', t.image_credit = 'N@ldo / CC BY-SA 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:ImariA.JPG'
 WHERE p.name = '사가현' AND t.name = '아리타 도자기 마을' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Y%C5%ABtoku_Inari_Shrine_sanmon_and_main_hall.JPG/960px-Y%C5%ABtoku_Inari_Shrine_sanmon_and_main_hall.JPG', t.image_credit = 'Underbar dk / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Y%C5%ABtoku_Inari_Shrine_sanmon_and_main_hall.JPG'
 WHERE p.name = '사가현' AND t.name = '유토쿠 이나리 신사' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/Huis_Ten_Bosch_-_01.jpg/960px-Huis_Ten_Bosch_-_01.jpg', t.image_credit = 'JKT-c / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Huis_Ten_Bosch_-_01.jpg'
 WHERE p.name = '나가사키현' AND t.name = '하우스텐보스' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/12/Mt.Inasa_2010.jpg/960px-Mt.Inasa_2010.jpg', t.image_credit = 'ぱちょぴ / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mt.Inasa_2010.jpg'
 WHERE p.name = '나가사키현' AND t.name = '이나사야마 전망대' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a8/Nagasaki_Hashima_01-1.png/960px-Nagasaki_Hashima_01-1.png', t.image_credit = 'Hisagi (氷鷺) / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nagasaki_Hashima_01-1.png'
 WHERE p.name = '나가사키현' AND t.name = '군함도' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fa/Oura_Church_Nagasaki.JPG/960px-Oura_Church_Nagasaki.JPG', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Oura_Church_Nagasaki.JPG'
 WHERE p.name = '나가사키현' AND t.name = '오우라 천주당' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/a/a3/Nagasaki_glover_16835805_886671322b_o_d.jpg', t.image_credit = 'Flickr user: Kanko* from Nagasaki, Japan / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nagasaki_glover_16835805_886671322b_o_d.jpg'
 WHERE p.name = '나가사키현' AND t.name = '글로버 정원' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/9/9f/Kumamotojo.jpg', t.image_credit = 'Birdman at Japanese Wikipedia / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kumamotojo.jpg'
 WHERE p.name = '구마모토현' AND t.name = '구마모토성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ed/Mount_Nakadake_from_East_Hill_of_Kusasenrigahama.JPG/960px-Mount_Nakadake_from_East_Hill_of_Kusasenrigahama.JPG', t.image_credit = 'そらみみ / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mount_Nakadake_from_East_Hill_of_Kusasenrigahama.JPG'
 WHERE p.name = '구마모토현' AND t.name = '아소산 나카다케 화구' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/42/Kurokawa_Onsen_-%E6%B8%A9%E6%B3%89%E8%A1%97.jpg/960px-Kurokawa_Onsen_-%E6%B8%A9%E6%B3%89%E8%A1%97.jpg', t.image_credit = 'hiroooooki from Tokyo, Japan / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kurokawa_Onsen_-%E6%B8%A9%E6%B3%89%E8%A1%97.jpg'
 WHERE p.name = '구마모토현' AND t.name = '구로카와 온천' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/85/Kumamoto_Suizenji-jojuen01n4272.jpg/960px-Kumamoto_Suizenji-jojuen01n4272.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kumamoto_Suizenji-jojuen01n4272.jpg'
 WHERE p.name = '구마모토현' AND t.name = '스이젠지 조주엔' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Beppu_Chinoike-jigoku03n3200.jpg/960px-Beppu_Chinoike-jigoku03n3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Beppu_Chinoike-jigoku03n3200.jpg'
 WHERE p.name = '오이타현' AND t.name = '벳푸 지옥 순례' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Yufuin_Onsen_-Mus%C5%8Den_02.jpg/960px-Yufuin_Onsen_-Mus%C5%8Den_02.jpg', t.image_credit = 'Yasuhiro from Tokyo, Japan / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Yufuin_Onsen_-Mus%C5%8Den_02.jpg'
 WHERE p.name = '오이타현' AND t.name = '유후인' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ad/Usa_Shrine_Nanchu_Romon.JPG/960px-Usa_Shrine_Nanchu_Romon.JPG', t.image_credit = 'ja:User:Sanjo / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Usa_Shrine_Nanchu_Romon.JPG'
 WHERE p.name = '오이타현' AND t.name = '우사 신궁' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Takachiho-kyo%28Gorge%29_-_River_-_%E5%B7%9D.jpg/960px-Takachiho-kyo%28Gorge%29_-_River_-_%E5%B7%9D.jpg', t.image_credit = 'TANAKA Juuyoh (田中十洋) / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Takachiho-kyo(Gorge)_-_River_-_%E5%B7%9D.jpg'
 WHERE p.name = '미야자키현' AND t.name = '다카치호 협곡' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Aoshima_jinja%2C_Worship_Hall_01.jpg/960px-Aoshima_jinja%2C_Worship_Hall_01.jpg', t.image_credit = 'Naokijp / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Aoshima_jinja,_Worship_Hall_01.jpg'
 WHERE p.name = '미야자키현' AND t.name = '아오시마 신사' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/74/Udo-jingu_Main-shrine.jpg/960px-Udo-jingu_Main-shrine.jpg', t.image_credit = 'MaedaAkihiko / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Udo-jingu_Main-shrine.jpg'
 WHERE p.name = '미야자키현' AND t.name = '우도 신궁' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/a/ac/Kagoshima_and_Sakurajima.jpg', t.image_credit = 'Takobou at Japanese Wikipedia / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kagoshima_and_Sakurajima.jpg'
 WHERE p.name = '가고시마현' AND t.name = '사쿠라지마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fe/Sengan-en2.jpg/960px-Sengan-en2.jpg', t.image_credit = 'STA3816 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sengan-en2.jpg'
 WHERE p.name = '가고시마현' AND t.name = '센간엔' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/36/Yakushima_from_Ferry_Amami_20121116.jpg/960px-Yakushima_from_Ferry_Amami_20121116.jpg', t.image_credit = 'tsuda from Tsushima, Aichi, Japan / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Yakushima_from_Ferry_Amami_20121116.jpg'
 WHERE p.name = '가고시마현' AND t.name = '야쿠시마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/Ibusuki.jpg/960px-Ibusuki.jpg', t.image_credit = 'Trinida / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ibusuki.jpg'
 WHERE p.name = '가고시마현' AND t.name = '이부스키 모래찜질' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/Kirishima-Jingu_Front.jpg/960px-Kirishima-Jingu_Front.jpg', t.image_credit = 'MaedaAkihiko / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kirishima-Jingu_Front.jpg'
 WHERE p.name = '가고시마현' AND t.name = '기리시마 신궁' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Naha_Shuri_Castle32bs5s4592.jpg/960px-Naha_Shuri_Castle32bs5s4592.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Naha_Shuri_Castle32bs5s4592.jpg'
 WHERE p.name = '오키나와현' AND t.name = '슈리성' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Kokusai-dori07ss4272.jpg/960px-Kokusai-dori07ss4272.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kokusai-dori07ss4272.jpg'
 WHERE p.name = '오키나와현' AND t.name = '국제거리' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/%E6%B2%96%E7%B8%84%E7%BE%8E%E3%82%89%E6%B5%B7%E6%B0%B4%E6%97%8F%E9%A4%A8%EF%BC%BF%E5%A4%A7.jpg/960px-%E6%B2%96%E7%B8%84%E7%BE%8E%E3%82%89%E6%B5%B7%E6%B0%B4%E6%97%8F%E9%A4%A8%EF%BC%BF%E5%A4%A7.jpg', t.image_credit = 'sinnosuke uchida / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E6%B2%96%E7%B8%84%E7%BE%8E%E3%82%89%E6%B5%B7%E6%B0%B4%E6%97%8F%E9%A4%A8%EF%BC%BF%E5%A4%A7.jpg'
 WHERE p.name = '오키나와현' AND t.name = '추라우미 수족관' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Onna_Okinawa_Japan_Cape-Manzamo-01.jpg/960px-Onna_Okinawa_Japan_Cape-Manzamo-01.jpg', t.image_credit = 'CEphoto, Uwe Aranas / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Onna_Okinawa_Japan_Cape-Manzamo-01.jpg'
 WHERE p.name = '오키나와현' AND t.name = '만자모' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/%E5%8F%A4%E5%AE%87%E5%88%A9%E5%A4%A7%E6%A9%8B_%E5%8F%A4%E5%AE%87%E5%88%A9%E3%83%93%E3%83%BC%E3%83%81_20100814a.jpg/960px-%E5%8F%A4%E5%AE%87%E5%88%A9%E5%A4%A7%E6%A9%8B_%E5%8F%A4%E5%AE%87%E5%88%A9%E3%83%93%E3%83%BC%E3%83%81_20100814a.jpg', t.image_credit = 'hasano_jp / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%8F%A4%E5%AE%87%E5%88%A9%E5%A4%A7%E6%A9%8B_%E5%8F%A4%E5%AE%87%E5%88%A9%E3%83%93%E3%83%BC%E3%83%81_20100814a.jpg'
 WHERE p.name = '오키나와현' AND t.name = '고우리 대교' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/66/Sangui_%28Triangle_Rock%29_of_Sefa-Utaki.jpg/960px-Sangui_%28Triangle_Rock%29_of_Sefa-Utaki.jpg', t.image_credit = 'そらみみ / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sangui_(Triangle_Rock)_of_Sefa-Utaki.jpg'
 WHERE p.name = '오키나와현' AND t.name = '세이파 우타키' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/90/Kabira-Bay-Kabira-park-2019.jpg/960px-Kabira-Bay-Kabira-park-2019.jpg', t.image_credit = 'Tokyo-Good / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kabira-Bay-Kabira-park-2019.jpg'
 WHERE p.name = '오키나와현' AND t.name = '이시가키섬 가비라만' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/02/Miyako_maipama_1.jpg/960px-Miyako_maipama_1.jpg', t.image_credit = 'Paipateroma / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Miyako_maipama_1.jpg'
 WHERE p.name = '오키나와현' AND t.name = '미야코섬 요나하 마에하마' AND t.image_path IS NULL;
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/09/Mihama_AV_D_Japan.jpg/960px-Mihama_AV_D_Japan.jpg', t.image_credit = 'Amoriver Information / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mihama_AV_D_Japan.jpg'
 WHERE p.name = '오키나와현' AND t.name = '아메리칸 빌리지' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4b/%E5%B0%91%E6%9E%97%E5%B1%B1%E9%81%94%E7%A3%A8%E5%AF%BA%E9%9C%8A%E7%AC%A6%E5%A0%82.JPG/960px-%E5%B0%91%E6%9E%97%E5%B1%B1%E9%81%94%E7%A3%A8%E5%AF%BA%E9%9C%8A%E7%AC%A6%E5%A0%82.JPG', t.image_credit = '小池 隆 / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%B0%91%E6%9E%97%E5%B1%B1%E9%81%94%E7%A3%A8%E5%AF%BA%E9%9C%8A%E7%AC%A6%E5%A0%82.JPG'
 WHERE p.name = '군마현' AND t.name = '다카사키 달마 시장' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bb/WakakusaYama.jpg/960px-WakakusaYama.jpg', t.image_credit = 'PlusMinus / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:WakakusaYama.jpg'
 WHERE p.name = '나라현' AND t.name = '와카쿠사야마 야마야키' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/Nozawa_Onsen_01.jpg/960px-Nozawa_Onsen_01.jpg', t.image_credit = 'Hideyuki KAMON / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nozawa_Onsen_01.jpg'
 WHERE p.name = '나가노현' AND t.name = '노자와 온천 도소진 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Sounkyo_Promenade.JPG/960px-Sounkyo_Promenade.JPG', t.image_credit = 'Soica2001 (talk) / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sounkyo_Promenade.JPG'
 WHERE p.name = '홋카이도' AND t.name = '소운쿄 빙폭 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9d/Mount_Yae_from_Mount_Awa_2018.JPG/960px-Mount_Yae_from_Mount_Awa_2018.JPG', t.image_credit = 'Kugel~commonswiki / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mount_Yae_from_Mount_Awa_2018.JPG'
 WHERE p.name = '오키나와현' AND t.name = '모토부 야에다케 벚꽃 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Odori_Park_Sapporo_Snow_Festival_2007.JPG/960px-Odori_Park_Sapporo_Snow_Festival_2007.JPG', t.image_credit = 'Eckhard Pecher / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Odori_Park_Sapporo_Snow_Festival_2007.JPG'
 WHERE p.name = '홋카이도' AND t.name = '삿포로 눈축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Otaru_yukiakari_no_michi.jpg/960px-Otaru_yukiakari_no_michi.jpg', t.image_credit = '欅 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Otaru_yukiakari_no_michi.jpg'
 WHERE p.name = '홋카이도' AND t.name = '오타루 눈빛 거리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/b/b3/Kamakura-yuki.jpg', t.image_credit = '（社）横手市観光協会 / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kamakura-yuki.jpg'
 WHERE p.name = '아키타현' AND t.name = '요코테 가마쿠라' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3e/Hadaka_Matsuri_%28-Naked_Festival-%29_festivities_in_Saidaiji%2C_Japan.jpg/960px-Hadaka_Matsuri_%28-Naked_Festival-%29_festivities_in_Saidaiji%2C_Japan.jpg', t.image_credit = 'Mstyslav Chernov/Unframe/http://www.unframe.com/ / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hadaka_Matsuri_(-Naked_Festival-)_festivities_in_Saidaiji,_Japan.jpg'
 WHERE p.name = '오카야마현' AND t.name = '사이다이지 에요 하다카 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Nagasaki_Lantern_Festival_-_01.jpg/960px-Nagasaki_Lantern_Festival_-_01.jpg', t.image_credit = 'JKT-c / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nagasaki_Lantern_Festival_-_01.jpg'
 WHERE p.name = '나가사키현' AND t.name = '나가사키 랜턴 페스티벌' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d9/%E6%B2%B3%E6%B4%A5%E6%A1%9C.jpg/960px-%E6%B2%B3%E6%B4%A5%E6%A1%9C.jpg', t.image_credit = 'Σ64 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E6%B2%B3%E6%B4%A5%E6%A1%9C.jpg'
 WHERE p.name = '시즈오카현' AND t.name = '가와즈 벚꽃 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Kairaku-en%2C_Ibaraki_24.jpg/960px-Kairaku-en%2C_Ibaraki_24.jpg', t.image_credit = 'Σ64 / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kairaku-en,_Ibaraki_24.jpg'
 WHERE p.name = '이바라키현' AND t.name = '미토 매화 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/Todaiji_Syunie_Nara_JPN_001.JPG/960px-Todaiji_Syunie_Nara_JPN_001.JPG', t.image_credit = 'ignis / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Todaiji_Syunie_Nara_JPN_001.JPG'
 WHERE p.name = '나라현' AND t.name = '도다이지 오미즈토리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a3/Big-hinamatsuri%28tokushima%29_20190303_02.jpg/960px-Big-hinamatsuri%28tokushima%29_20190303_02.jpg', t.image_credit = 'Shizuha / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Big-hinamatsuri(tokushima)_20190303_02.jpg'
 WHERE p.name = '지바현' AND t.name = '가쓰우라 빅 히나 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/b/b2/%E4%BA%AC%E9%83%BD%E3%83%BB%E6%9D%B1%E5%B1%B1%E8%8A%B1%E7%81%AF%E8%B7%AF%E6%B8%85%E6%B0%B4%E5%9D%82%E8%A1%8C%E7%81%AF.jpg', t.image_credit = '倶楽部オータム / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E4%BA%AC%E9%83%BD%E3%83%BB%E6%9D%B1%E5%B1%B1%E8%8A%B1%E7%81%AF%E8%B7%AF%E6%B8%85%E6%B0%B4%E5%9D%82%E8%A1%8C%E7%81%AF.jpg'
 WHERE p.name = '교토부' AND t.name = '교토 히가시야마 하나토로' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/Himure_hachimangu04s3200.jpg/960px-Himure_hachimangu04s3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Himure_hachimangu04s3200.jpg'
 WHERE p.name = '시가현' AND t.name = '오미하치만 사기초 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/Kagura_Tai_Float.jpg/960px-Kagura_Tai_Float.jpg', t.image_credit = 'Bernard Gagnon / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kagura_Tai_Float.jpg'
 WHERE p.name = '기후현' AND t.name = '다카야마 마쓰리 (봄)' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/Hirosaki_castle.jpg/960px-Hirosaki_castle.jpg', t.image_credit = 'Angaurits / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hirosaki_castle.jpg'
 WHERE p.name = '아오모리현' AND t.name = '히로사키 벚꽃 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Maikos_dancing_at_Miyako_odori.jpg/960px-Maikos_dancing_at_Miyako_odori.jpg', t.image_credit = 'Steven Rolland from Osaka, Japan / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Maikos_dancing_at_Miyako_odori.jpg'
 WHERE p.name = '교토부' AND t.name = '미야코 오도리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/40/Inuyamajo2.JPG/960px-Inuyamajo2.JPG', t.image_credit = 'Bariston / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Inuyamajo2.JPG'
 WHERE p.name = '아이치현' AND t.name = '이누야마 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/37/Nagahama_City_Library_2020-03_ac_%282%29.jpg/960px-Nagahama_City_Library_2020-03_ac_%282%29.jpg', t.image_credit = 'Asturio Cantabrio / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nagahama_City_Library_2020-03_ac_(2).jpg'
 WHERE p.name = '시가현' AND t.name = '나가하마 히키야마 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/61/Shingen-ko_Festival_20260404.jpg/960px-Shingen-ko_Festival_20260404.jpg', t.image_credit = '武田信徳 / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Shingen-ko_Festival_20260404.jpg'
 WHERE p.name = '야마나시현' AND t.name = '신겐코 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d1/Beppu_Tower02s4s3200.jpg/960px-Beppu_Tower02s4s3200.jpg', t.image_credit = '663highland / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Beppu_Tower02s4s3200.jpg'
 WHERE p.name = '오이타현' AND t.name = '벳푸 하쓰온천 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/3/3e/Aoi-Matsuri-01.jpg', t.image_credit = 'J o / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Aoi-Matsuri-01.jpg'
 WHERE p.name = '교토부' AND t.name = '아오이 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/15/%E4%B8%89%E4%B9%8B%E5%AE%AE%E7%A5%9E%E8%BC%BF%E5%AE%AE%E5%87%BA%E3%81%97.JPG/960px-%E4%B8%89%E4%B9%8B%E5%AE%AE%E7%A5%9E%E8%BC%BF%E5%AE%AE%E5%87%BA%E3%81%97.JPG', t.image_credit = '江戸村のとくぞう (Edomura no Tokuzo) / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E4%B8%89%E4%B9%8B%E5%AE%AE%E7%A5%9E%E8%BC%BF%E5%AE%AE%E5%87%BA%E3%81%97.JPG'
 WHERE p.name = '도쿄도' AND t.name = '산자 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d0/Kanda_Matsuri_0716.JPG/960px-Kanda_Matsuri_0716.JPG', t.image_credit = 'Ciphers / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kanda_Matsuri_0716.JPG'
 WHERE p.name = '도쿄도' AND t.name = '간다 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/Hakata_Dontaku_78339165_org.jpg/960px-Hakata_Dontaku_78339165_org.jpg', t.image_credit = 'フェレス / CC BY-SA 2.1 jp / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hakata_Dontaku_78339165_org.jpg'
 WHERE p.name = '후쿠오카현' AND t.name = '하카타 돈타쿠' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/47/%E6%B5%9C%E6%9D%BE%E3%81%BE%E3%81%A4%E3%82%8A%E5%BE%A1%E6%AE%BF%E5%B1%8B%E5%8F%B0.JPG/960px-%E6%B5%9C%E6%9D%BE%E3%81%BE%E3%81%A4%E3%82%8A%E5%BE%A1%E6%AE%BF%E5%B1%8B%E5%8F%B0.JPG', t.image_credit = 'sakuzayama / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E6%B5%9C%E6%9D%BE%E3%81%BE%E3%81%A4%E3%82%8A%E5%BE%A1%E6%AE%BF%E5%B1%8B%E5%8F%B0.JPG'
 WHERE p.name = '시즈오카현' AND t.name = '하마마쓰 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b5/Nikko_Toshogu_Yomeimon_Gate_2024.jpg/960px-Nikko_Toshogu_Yomeimon_Gate_2024.jpg', t.image_credit = 'Jpatokal / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nikko_Toshogu_Yomeimon_Gate_2024.jpg'
 WHERE p.name = '도치기현' AND t.name = '닛코 도쇼구 봄 대제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e0/Mikuni_festival_2022.jpg/960px-Mikuni_festival_2022.jpg', t.image_credit = '藤谷良秀(Fujitani Yoshihide) / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Mikuni_festival_2022.jpg'
 WHERE p.name = '후쿠이현' AND t.name = '미쿠니 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/FF2005_hana_no_to.JPG/960px-FF2005_hana_no_to.JPG', t.image_credit = 'Taisyo / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:FF2005_hana_no_to.JPG'
 WHERE p.name = '히로시마현' AND t.name = '히로시마 플라워 페스티벌' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e7/YOSAKOI_Soran_Festival.jpg/960px-YOSAKOI_Soran_Festival.jpg', t.image_credit = 'もんもん at Japanese Wikipedia / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:YOSAKOI_Soran_Festival.jpg'
 WHERE p.name = '홋카이도' AND t.name = 'YOSAKOI 소란 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/eb/Hyakumangoku_Matsuri_in_front_of_Kanazawa_station.jpg/960px-Hyakumangoku_Matsuri_in_front_of_Kanazawa_station.jpg', t.image_credit = 'Z3144228 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hyakumangoku_Matsuri_in_front_of_Kanazawa_station.jpg'
 WHERE p.name = '이시카와현' AND t.name = '가나자와 햐쿠만고쿠 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/da/Hie_jinjya-7.jpg/960px-Hie_jinjya-7.jpg', t.image_credit = '江戸村のとくぞう (Edomura no Tokuzo) / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hie_jinjya-7.jpg'
 WHERE p.name = '도쿄도' AND t.name = '산노 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Chagu-chagu_Umakko_parade_in_front_of_Morioka_Station_2023a.jpg/960px-Chagu-chagu_Umakko_parade_in_front_of_Morioka_Station_2023a.jpg', t.image_credit = '掬茶 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Chagu-chagu_Umakko_parade_in_front_of_Morioka_Station_2023a.jpg'
 WHERE p.name = '이와테현' AND t.name = '차구차구 우마코' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/5/54/%E3%82%B9%E3%82%A4%E3%83%83%E3%83%81%E3%83%90%E3%83%83%E3%82%AF%E5%BC%8F%E4%BF%A1%E5%8F%B7%E5%A0%B4%EF%BC%88%E7%AE%B1%E6%A0%B9%E7%99%BB%E5%B1%B1%E9%89%84%E9%81%93%EF%BC%89.png', t.image_credit = 'ほくほく超高速進行 / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E3%82%B9%E3%82%A4%E3%83%83%E3%83%81%E3%83%90%E3%83%83%E3%82%AF%E5%BC%8F%E4%BF%A1%E5%8F%B7%E5%A0%B4%EF%BC%88%E7%AE%B1%E6%A0%B9%E7%99%BB%E5%B1%B1%E9%89%84%E9%81%93%EF%BC%89.png'
 WHERE p.name = '가나가와현' AND t.name = '하코네 아지사이 전차' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/Kyoto_Gion_Matsuri_J09_010.jpg/960px-Kyoto_Gion_Matsuri_J09_010.jpg', t.image_credit = 'Corpse Reviver / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kyoto_Gion_Matsuri_J09_010.jpg'
 WHERE p.name = '교토부' AND t.name = '기온 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/8/8b/H17_daikokunagare.jpg', t.image_credit = '枝折 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:H17_daikokunagare.jpg'
 WHERE p.name = '후쿠오카현' AND t.name = '하카타 기온 야마카사' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/35/Tenjinmatsuri.JPG/960px-Tenjinmatsuri.JPG', t.image_credit = 'Midori / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tenjinmatsuri.JPG'
 WHERE p.name = '오사카부' AND t.name = '덴진 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2e/2012%E5%B9%B4%E9%9A%85%E7%94%B0%E5%B7%9D%E8%8A%B1%E7%81%AB%E5%A4%A7%E4%BC%9A.jpg/960px-2012%E5%B9%B4%E9%9A%85%E7%94%B0%E5%B7%9D%E8%8A%B1%E7%81%AB%E5%A4%A7%E4%BC%9A.jpg', t.image_credit = 'てんどん / CC BY 2.1 jp / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:2012%E5%B9%B4%E9%9A%85%E7%94%B0%E5%B7%9D%E8%8A%B1%E7%81%AB%E5%A4%A7%E4%BC%9A.jpg'
 WHERE p.name = '도쿄도' AND t.name = '스미다가와 불꽃 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/1/15/The_Soma_Nomaoi_2005-5.jpg', t.image_credit = 'PekePON / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:The_Soma_Nomaoi_2005-5.jpg'
 WHERE p.name = '후쿠시마현' AND t.name = '소마 노마오이' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/Nachi_Falls_2025-10-13.jpg/960px-Nachi_Falls_2025-10-13.jpg', t.image_credit = 'Alpsdake / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nachi_Falls_2025-10-13.jpg'
 WHERE p.name = '와카야마현' AND t.name = '나치 불 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Nebuta2010.JPG/960px-Nebuta2010.JPG', t.image_credit = 'Kitamura,Yamato / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nebuta2010.JPG'
 WHERE p.name = '아오모리현' AND t.name = '아오모리 네부타 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/66/Kan_h03.jpg/960px-Kan_h03.jpg', t.image_credit = '秋田市 / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kan_h03.jpg'
 WHERE p.name = '아키타현' AND t.name = '아키타 간토 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/81/Sendai_Tanabata_2023.jpg/960px-Sendai_Tanabata_2023.jpg', t.image_credit = '掬茶 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sendai_Tanabata_2023.jpg'
 WHERE p.name = '미야기현' AND t.name = '센다이 칠석 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Yamagata_Hanagasa_Festival_in_Yamagata-station2025.jpg/960px-Yamagata_Hanagasa_Festival_in_Yamagata-station2025.jpg', t.image_credit = 'Suz-b / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Yamagata_Hanagasa_Festival_in_Yamagata-station2025.jpg'
 WHERE p.name = '야마가타현' AND t.name = '야마가타 하나가사 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/9/91/Sansa_Odori_2.JPG', t.image_credit = '작자 미상 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Sansa_Odori_2.JPG'
 WHERE p.name = '이와테현' AND t.name = '모리오카 산사 오도리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/94/Awa-odori_2008_Tokushima.jpg/960px-Awa-odori_2008_Tokushima.jpg', t.image_credit = 'Rosino / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Awa-odori_2008_Tokushima.jpg'
 WHERE p.name = '도쿠시마현' AND t.name = '아와오도리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ab/Hamakoi_Dance_Festival_2009_at_the_Yokohama_Sogo_department_store.jpg/960px-Hamakoi_Dance_Festival_2009_at_the_Yokohama_Sogo_department_store.jpg', t.image_credit = 'Kounosu / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hamakoi_Dance_Festival_2009_at_the_Yokohama_Sogo_department_store.jpg'
 WHERE p.name = '고치현' AND t.name = '요사코이 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/12/Find47_Niigata-The_Nagaoka_Fireworks_Festival-m2.jpg/960px-Find47_Niigata-The_Nagaoka_Fireworks_Festival-m2.jpg', t.image_credit = 'Koichi_Hayakawa / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Find47_Niigata-The_Nagaoka_Fireworks_Festival-m2.jpg'
 WHERE p.name = '니가타현' AND t.name = '나가오카 불꽃 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Gozanokuribi_Daimonji3.JPG/960px-Gozanokuribi_Daimonji3.JPG', t.image_credit = '佐野宇久井 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Gozanokuribi_Daimonji3.JPG'
 WHERE p.name = '교토부' AND t.name = '교토 고잔 오쿠리비' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ed/Gujo_odori_2009-08-16.jpg/960px-Gujo_odori_2009-08-16.jpg', t.image_credit = 'tsuda / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Gujo_odori_2009-08-16.jpg'
 WHERE p.name = '기후현' AND t.name = '구조 오도리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/5/52/Seragaki_eisa.jpg', t.image_credit = 'Kasanui]＆KI] / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Seragaki_eisa.jpg'
 WHERE p.name = '오키나와현' AND t.name = '오키나와 전도 에이사 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/24/Yamaga-Lantern.JPG/960px-Yamaga-Lantern.JPG', t.image_credit = 'NY066 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Yamaga-Lantern.JPG'
 WHERE p.name = '구마모토현' AND t.name = '야마가 등롱 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/36/IshidoriMatsuri.JPG/960px-IshidoriMatsuri.JPG', t.image_credit = 'Outside147 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:IshidoriMatsuri.JPG'
 WHERE p.name = '미에현' AND t.name = '구와나 이시도리 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cb/%E9%96%A2%E9%96%80%E6%B5%B7%E5%B3%A1%E8%8A%B1%E7%81%AB%E5%A4%A7%E4%BC%9A2018-%E3%83%95%E3%82%A3%E3%83%8A%E3%83%BC%E3%83%AC.webm/960px--%E9%96%A2%E9%96%80%E6%B5%B7%E5%B3%A1%E8%8A%B1%E7%81%AB%E5%A4%A7%E4%BC%9A2018-%E3%83%95%E3%82%A3%E3%83%8A%E3%83%BC%E3%83%AC.webm.jpg', t.image_credit = '綾祢は今日も旅をする / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E9%96%A2%E9%96%80%E6%B5%B7%E5%B3%A1%E8%8A%B1%E7%81%AB%E5%A4%A7%E4%BC%9A2018-%E3%83%95%E3%82%A3%E3%83%8A%E3%83%BC%E3%83%AC.webm'
 WHERE p.name = '야마구치현' AND t.name = '시모노세키 해협 불꽃 축제' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Japan_-_Takamatsu_Awa_Odori_Bon_Festival_03.jpg/960px-Japan_-_Takamatsu_Awa_Odori_Bon_Festival_03.jpg', t.image_credit = 'Vanvelthem Cédric / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Japan_-_Takamatsu_Awa_Odori_Bon_Festival_03.jpg'
 WHERE p.name = '가가와현' AND t.name = '다카마쓰 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8e/Shanshan2013-4.jpg/960px-Shanshan2013-4.jpg', t.image_credit = 'KASEI / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Shanshan2013-4.jpg'
 WHERE p.name = '돗토리현' AND t.name = '돗토리 시안 가사오도리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/d/dd/Kazenobon01.jpg', t.image_credit = 'Jinzuu / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kazenobon01.jpg'
 WHERE p.name = '도야마현' AND t.name = '오와라 가제노본' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/0/02/Kishiwada_danjiri.jpg', t.image_credit = 'Idushin at Japanese Wikipedia / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kishiwada_danjiri.jpg'
 WHERE p.name = '오사카부' AND t.name = '기시와다 단지리 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d1/TsurugaokaHachimangu_by_ulysses_powers_in_Kamakura.jpg/960px-TsurugaokaHachimangu_by_ulysses_powers_in_Kamakura.jpg', t.image_credit = 'ulysses_powers from Tsurugaoka Hachimangu in Yukinoshita, Kamakura, Kanagawa / CC BY-SA 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:TsurugaokaHachimangu_by_ulysses_powers_in_Kamakura.jpg'
 WHERE p.name = '가나가와현' AND t.name = '쓰루가오카 하치만구 야부사메' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c6/Kakunodate_maturi_2008a.jpg/960px-Kakunodate_maturi_2008a.jpg', t.image_credit = 'Soica2001 (talk) / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kakunodate_maturi_2008a.jpg'
 WHERE p.name = '아키타현' AND t.name = '가쿠노다테 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/Kagura_Tai_Float.jpg/960px-Kagura_Tai_Float.jpg', t.image_credit = 'Bernard Gagnon / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kagura_Tai_Float.jpg'
 WHERE p.name = '기후현' AND t.name = '다카야마 마쓰리 (가을)' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/87/Jaodori_of_Nagasaki_Kunchi.jpg/960px-Jaodori_of_Nagasaki_Kunchi.jpg', t.image_credit = 'Marine-Blue / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Jaodori_of_Nagasaki_Kunchi.jpg'
 WHERE p.name = '나가사키현' AND t.name = '나가사키 군치' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/c/ca/Jidai_Matsuri-Yoshino_Period_1.jpg', t.image_credit = 'Cookie4869 / CC BY 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Jidai_Matsuri-Yoshino_Period_1.jpg'
 WHERE p.name = '교토부' AND t.name = '지다이 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f4/Kawagoe_Festival4.jpg/960px-Kawagoe_Festival4.jpg', t.image_credit = '江戸村のとくぞう / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Kawagoe_Festival4.jpg'
 WHERE p.name = '사이타마현' AND t.name = '가와고에 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/84/Nada_no_Kenka_matsuri_%2C_himeji01.JPG/960px-Nada_no_Kenka_matsuri_%2C_himeji01.JPG', t.image_credit = 'sailko / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Nada_no_Kenka_matsuri_,_himeji01.JPG'
 WHERE p.name = '효고현' AND t.name = '나다 겐카 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/Naha_Tug_of_War.jpg/960px-Naha_Tug_of_War.jpg', t.image_credit = '那覇市観光協会 / CC BY-SA 2.1 jp / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Naha_Tug_of_War.jpg'
 WHERE p.name = '오키나와현' AND t.name = '나하 대줄다리기' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/87/Saijou_danjiri.JPG/960px-Saijou_danjiri.JPG', t.image_credit = 'ＨＮ：依存性だんぢり中毒 / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Saijou_danjiri.JPG'
 WHERE p.name = '에히메현' AND t.name = '사이조 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/64/Hikiyama.jpg/960px-Hikiyama.jpg', t.image_credit = '작자 미상 / CC BY-SA 2.5 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hikiyama.jpg'
 WHERE p.name = '사가현' AND t.name = '가라쓰 군치' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/La_Montgorlfier_Nocturne.JPG/960px-La_Montgorlfier_Nocturne.JPG', t.image_credit = 'Ntoshi / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:La_Montgorlfier_Nocturne.JPG'
 WHERE p.name = '사가현' AND t.name = '사가 인터내셔널 벌룬 페스타' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/58/Tsuchiura_Fireworks_2014.jpg/960px-Tsuchiura_Fireworks_2014.jpg', t.image_credit = '河田 貫成 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tsuchiura_Fireworks_2014.jpg'
 WHERE p.name = '이바라키현' AND t.name = '쓰치우라 전국 불꽃 경기대회' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Hanazono_shrine_tori_no_ichi_fair20251124.jpg/960px-Hanazono_shrine_tori_no_ichi_fair20251124.jpg', t.image_credit = 'Mozzucu / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Hanazono_shrine_tori_no_ichi_fair20251124.jpg'
 WHERE p.name = '도쿄도' AND t.name = '도리노이치' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Arashiyama_%288741759830%29.jpg/960px-Arashiyama_%288741759830%29.jpg', t.image_credit = 'Takeshi Kuboki from Amagasaki, Japan / CC BY 2.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Arashiyama_(8741759830).jpg'
 WHERE p.name = '교토부' AND t.name = '아라시야마 모미지 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/76/NDL1307403_%E5%BE%A1%E5%A4%A7%E5%90%8D%E8%A1%8C%E5%88%97%E4%B9%8B%E5%9B%B3.jpg/960px-NDL1307403_%E5%BE%A1%E5%A4%A7%E5%90%8D%E8%A1%8C%E5%88%97%E4%B9%8B%E5%9B%B3.jpg', t.image_credit = '哥川豊春 / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:NDL1307403_%E5%BE%A1%E5%A4%A7%E5%90%8D%E8%A1%8C%E5%88%97%E4%B9%8B%E5%9B%B3.jpg'
 WHERE p.name = '가나가와현' AND t.name = '하코네 다이묘 행렬' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/0/04/Ohara_festival_in_Kagoshima.jpg', t.image_credit = 'Kagoshima City / CC BY 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Ohara_festival_in_Kagoshima.jpg'
 WHERE p.name = '가고시마현' AND t.name = '오하라 마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1c/Haiden_of_Izumo-taisha-1.JPG/960px-Haiden_of_Izumo-taisha-1.JPG', t.image_credit = 'Saigen Jiro / CC0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Haiden_of_Izumo-taisha-1.JPG'
 WHERE p.name = '시마네현' AND t.name = '이즈모 타이샤 가미아리사이' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Yokagura.jpg/960px-Yokagura.jpg', t.image_credit = 'Takasunrise0921 / CC BY-SA 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Yokagura.jpg'
 WHERE p.name = '미야자키현' AND t.name = '다카치호 요카구라' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b9/ChichibuFes1.jpg/960px-ChichibuFes1.jpg', t.image_credit = 'ちちびあん / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:ChichibuFes1.jpg'
 WHERE p.name = '사이타마현' AND t.name = '지치부 요마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/62/%E7%AC%AC30%E5%9B%9E%E7%A5%9E%E6%88%B8%E3%83%AB%E3%83%9F%E3%83%8A%E3%83%AA%E3%82%A8%282%29.jpg/960px-%E7%AC%AC30%E5%9B%9E%E7%A5%9E%E6%88%B8%E3%83%AB%E3%83%9F%E3%83%8A%E3%83%AA%E3%82%A8%282%29.jpg', t.image_credit = 'Yoyogimura / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:%E7%AC%AC30%E5%9B%9E%E7%A5%9E%E6%88%B8%E3%83%AB%E3%83%9F%E3%83%8A%E3%83%AA%E3%82%A8(2).jpg'
 WHERE p.name = '효고현' AND t.name = '고베 루미나리에' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://upload.wikimedia.org/wikipedia/commons/a/ac/Onmatsuri7.jpeg', t.image_credit = 'Lovelin6th / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Onmatsuri7.jpeg'
 WHERE p.name = '나라현' AND t.name = '가스가 와카미야 온마쓰리' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/13/Tree_of_Hakodate_Christmas_Fantasy_in_sunset_time.jpg/960px-Tree_of_Hakodate_Christmas_Fantasy_in_sunset_time.jpg', t.image_credit = '掬茶 / CC BY-SA 4.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Tree_of_Hakodate_Christmas_Fantasy_in_sunset_time.jpg'
 WHERE p.name = '홋카이도' AND t.name = '하코다테 크리스마스 판타지' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/32/1cho_london1.jpg/960px-1cho_london1.jpg', t.image_credit = '작자 미상 / Public domain / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:1cho_london1.jpg'
 WHERE p.name = '도쿄도' AND t.name = '마루노우치 일루미네이션' AND t.image_path IS NULL;
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_path = 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/Huis_Ten_Bosch_-_01.jpg/960px-Huis_Ten_Bosch_-_01.jpg', t.image_credit = 'JKT-c / CC BY 3.0 / Wikimedia Commons', t.image_source = 'https://commons.wikimedia.org/wiki/File:Huis_Ten_Bosch_-_01.jpg'
 WHERE p.name = '나가사키현' AND t.name = '하우스텐보스 빛의 왕국' AND t.image_path IS NULL;

-- 출처 문구 정리 (D-081). 같은 사진(image_source)의 정리 전 출처만 바꾼다 — 다시 돌려도 같다.
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_credit = 'Fg2 / Public domain / Wikimedia Commons'
 WHERE p.name = '도쿄도' AND t.name = '도쿄 타워' AND t.image_source = 'https://commons.wikimedia.org/wiki/File:Tokyo_Tower_M4854.jpg' AND t.image_credit = 'No machine-readable author provided. Fg2 assumed (based on copyright claims). / Public domain / Wikimedia Commons';
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_credit = 'N yotarou / CC BY 2.5 / Wikimedia Commons'
 WHERE p.name = '미에현' AND t.name = '이세 신궁' AND t.image_source = 'https://commons.wikimedia.org/wiki/File:Naiku_01.JPG' AND t.image_credit = 'No machine-readable author provided. N yotarou assumed (based on copyright claims). / CC BY 2.5 / Wikimedia Commons';
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_credit = 'おぉたむすねィく探検隊 / CC BY-SA 3.0 / Wikimedia Commons'
 WHERE p.name = '돗토리현' AND t.name = '미즈키 시게루 로드' AND t.image_source = 'https://commons.wikimedia.org/wiki/File:%E5%A2%83%E6%B8%AF%E6%9C%AC%E7%94%BA%E3%82%A2%E3%83%BC%E3%82%B1%E3%83%BC%E3%83%89%E5%95%86%E5%BA%97%E8%A1%972.JPG' AND t.image_credit = 'No machine-readable author provided. おぉたむすねィく探検隊 assumed (based on copyright claims). / CC BY-SA 3.0 / Wikimedia Commons';
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_credit = 'Hokanomono / CC BY-SA 3.0 / Wikimedia Commons'
 WHERE p.name = '고치현' AND t.name = '시만토강' AND t.image_source = 'https://commons.wikimedia.org/wiki/File:Shimantogawa_2000-08.jpeg' AND t.image_credit = 'No machine-readable author provided. Hokanomono assumed (based on copyright claims). / CC BY-SA 3.0 / Wikimedia Commons';
UPDATE destinations t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_credit = 'N@ldo / CC BY-SA 2.5 / Wikimedia Commons'
 WHERE p.name = '사가현' AND t.name = '아리타 도자기 마을' AND t.image_source = 'https://commons.wikimedia.org/wiki/File:ImariA.JPG' AND t.image_credit = 'No machine-readable author provided. N@ldo assumed (based on copyright claims). / CC BY-SA 2.5 / Wikimedia Commons';
UPDATE festivals t JOIN prefectures p ON p.id = t.prefecture_id
   SET t.image_credit = '작자 미상 / Public domain / Wikimedia Commons'
 WHERE p.name = '도쿄도' AND t.name = '마루노우치 일루미네이션' AND t.image_source = 'https://commons.wikimedia.org/wiki/File:1cho_london1.jpg' AND t.image_credit = 'Unknown authorUnknown author / Public domain / Wikimedia Commons';
