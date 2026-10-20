import os
from PIL import Image

def make_square(im, min_size=512, fill_color=(255, 255, 255, 255)):
    x, y = im.size
    size = max(min_size, x, y)
    new_im = Image.new('RGBA', (size, size), fill_color)
    new_im.paste(im, (int((size - x) / 2), int((size - y) / 2)))
    return new_im

def main():
    img_path = r"C:\Users\dell\.gemini\antigravity\brain\72a4dfaf-b833-4852-b3f4-01395c7fde5a\.user_uploaded\media__1784749243572.png"
    output_dir = r"D:\Projects\goliftngo-driver"

    if not os.path.exists(img_path):
        print("Image not found")
        return

    im = Image.open(img_path).convert('RGBA')

    # 1. App Icon: 512x512
    square_im = make_square(im, min_size=512, fill_color=(255, 255, 255, 255))
    icon_512 = square_im.resize((512, 512), Image.Resampling.LANCZOS)
    icon_path = os.path.join(output_dir, "play_store_icon_512.png")
    icon_512.save(icon_path, "PNG")
    print(f"Saved 512x512 icon to {icon_path}")

    # 2. Feature Graphic: 1024x500
    fg = Image.new('RGBA', (1024, 500), (255, 255, 255, 255))
    # Resize original logo to fit nicely in 1024x500 (e.g. max width 750)
    logo_w, logo_h = im.size
    target_w = 750
    target_h = int(logo_h * (target_w / logo_w))
    if target_h > 350:
        target_h = 350
        target_w = int(logo_w * (target_h / logo_h))
    
    resized_logo = im.resize((target_w, target_h), Image.Resampling.LANCZOS)
    
    pos_x = (1024 - target_w) // 2
    pos_y = (500 - target_h) // 2
    fg.paste(resized_logo, (pos_x, pos_y), resized_logo)
    
    fg_path = os.path.join(output_dir, "play_store_feature_graphic_1024x500.png")
    fg.save(fg_path, "PNG")
    print(f"Saved 1024x500 feature graphic to {fg_path}")

if __name__ == '__main__':
    main()
