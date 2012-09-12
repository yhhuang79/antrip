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
 * PHP �I�ϵ{��
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

/* Ū������ */
//$im = imagecreatefromjpeg($filename);

/* �Ϥ��n�I�h��, ��/�e */
/*$new_img_width  = 800;
$new_img_height = 600;

/* ���إߤ@�� �s���ťչ��� */
$newim = imagecreate($new_img_width, $new_img_height);

// ��X�ϭn�q����}�lx, y , ��l�ϭn�q����}�l x, y , �n�e�h�j x, y(resize) , �n��h�j x, y
imagecopyresampled($newim, $im, 0, 0, 50, 100, $new_img_width, $new_img_height, $new_img_width, $new_img_height);

/* ��j �� 500 x 500 ���� */
// imagecopyresampled($newim, $im, 0, 0, 100, 30, 500, 500, $new_img_width, $new_img_height);

/* �N�ϦL�X�� */
imagejpeg($newim);

/* �귽�^�� */
//imagedestroy($newim);
//imagedestroy($im);*/
?>