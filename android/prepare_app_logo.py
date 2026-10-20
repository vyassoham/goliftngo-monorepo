import os
import shutil
from PIL import Image

def make_square(im, min_size=256, fill_color=(255, 255, 255, 255)):
    x, y = im.size
    size = max(min_size, x, y)
    new_im = Image.new('RGBA', (size, size), fill_color)
    new_im.paste(im, (int((size - x) / 2), int((size - y) / 2)))
    return new_im

def main():
    img_path = r"C:\Users\dell\.gemini\antigravity\brain\72a4dfaf-b833-4852-b3f4-01395c7fde5a\.user_uploaded\media__1784749243572.png"
    res_path = r"D:\Projects\goliftngo-driver\app\src\main\res"
    
    if not os.path.exists(img_path):
        print(f"Error: Could not find image at {img_path}")
        return

    im = Image.open(img_path).convert('RGBA')

    # Copy full logo to drawable/logo.png
    drawable_dir = os.path.join(res_path, "drawable")
    if not os.path.exists(drawable_dir):
        os.makedirs(drawable_dir)
    
    logo_target = os.path.join(drawable_dir, "logo.png")
    im.save(logo_target, "PNG")
    print(f"Saved full logo to {logo_target}")

    # Make square version for launcher icons
    square_im = make_square(im, fill_color=(255, 255, 255, 255))
    
    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
        "mipmap-anydpi-v26": None
    }
    
    for folder, size in sizes.items():
        folder_path = os.path.join(res_path, folder)
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
            
        if size is None:
            xml1 = os.path.join(folder_path, "ic_launcher.xml")
            xml2 = os.path.join(folder_path, "ic_launcher_round.xml")
            if os.path.exists(xml1): os.remove(xml1)
            if os.path.exists(xml2): os.remove(xml2)
            continue
            
        resized = square_im.resize((size, size), Image.Resampling.LANCZOS)
        
        webp1 = os.path.join(folder_path, "ic_launcher.webp")
        webp2 = os.path.join(folder_path, "ic_launcher_round.webp")
        if os.path.exists(webp1): os.remove(webp1)
        if os.path.exists(webp2): os.remove(webp2)
        
        resized.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
        resized.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
        print(f"Saved {size}x{size} icon to {folder}")

if __name__ == '__main__':
    main()
