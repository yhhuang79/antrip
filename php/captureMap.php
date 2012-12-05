<?php
/**
 The MIT License

 Copyright (c) 2007 <Tsung-Hao>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 *
 * PHP 截圖程式
 *
 * @author: tsung http://plog.longwin.com.tw
 * @desc: http://plog.longwin.com.tw/programming/2007/11/09/php_snap_image_block_2007
 *
 */
//header("Content-type: image/jpeg");

//$filename = 'book_rabbit_rule.jpg';

//$im = imagegrabscreen();
//imagepng($im, "gd_screen.png");

//$filename =$im;

/* 讀取圖檔 */
//$im = imagecreatefromjpeg($filename);

/* 圖片要截多少, 長/寬 */
/*$new_img_width  = 800;
$new_img_height = 600;

/* 先建立一個 新的空白圖檔 */
$newim = imagecreate($new_img_width, $new_img_height);

// 輸出圖要從哪邊開始x, y , 原始圖要從哪邊開始 x, y , 要畫多大 x, y(resize) , 要抓多大 x, y
imagecopyresampled($newim, $im, 0, 0, 50, 100, $new_img_width, $new_img_height, $new_img_width, $new_img_height);

/* 放大 成 500 x 500 的圖 */
// imagecopyresampled($newim, $im, 0, 0, 100, 30, 500, 500, $new_img_width, $new_img_height);

/* 將圖印出來 */
imagejpeg($newim);

/* 資源回收 */
//imagedestroy($newim);
//imagedestroy($im);*/
?>