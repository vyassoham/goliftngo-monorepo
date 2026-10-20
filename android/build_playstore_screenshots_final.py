import os
from PIL import Image, ImageDraw, ImageFont

def generate_screenshots():
    desktop = os.path.expanduser('~/Desktop')
    logo_path = r"C:\Users\dell\.gemini\antigravity\brain\72a4dfaf-b833-4852-b3f4-01395c7fde5a\.user_uploaded\media__1784749243572.png"
    logo = Image.open(logo_path).convert('RGBA')
    lw, lh = logo.size

    def get_header_logo(target_w=460):
        target_h = int(lh * (target_w / lw))
        return logo.resize((target_w, target_h), Image.Resampling.LANCZOS)

    # -------------------------------------------------------------
    # Screenshot 1: Driver Login Screen (1080 x 1920)
    # -------------------------------------------------------------
    s1 = Image.new('RGB', (1080, 1920), (255, 255, 255))
    d1 = ImageDraw.Draw(s1)
    
    # System Status Bar
    d1.rectangle([0, 0, 1080, 70], fill=(10, 37, 64))
    
    # Web App Header
    d1.rectangle([0, 70, 1080, 220], fill=(255, 255, 255))
    d1.line([(0, 220), (1080, 220)], fill=(230, 235, 240), width=2)
    hl = get_header_logo(460)
    s1.paste(hl, ((1080 - 460) // 2, 95), hl)

    # Login Box
    d1.rounded_rectangle([70, 280, 1010, 1450], radius=24, fill=(255, 255, 255), outline=(215, 222, 232), width=2)
    d1.rounded_rectangle([120, 340, 960, 420], radius=12, fill=(240, 245, 255))
    
    # Inputs
    d1.rectangle([120, 480, 360, 510], fill=(100, 110, 125))
    d1.rounded_rectangle([120, 530, 960, 640], radius=14, fill=(248, 250, 253), outline=(195, 205, 218), width=2)
    d1.rectangle([160, 570, 460, 600], fill=(150, 160, 175))
    
    d1.rectangle([120, 680, 320, 710], fill=(100, 110, 125))
    d1.rounded_rectangle([120, 730, 960, 840], radius=14, fill=(248, 250, 253), outline=(195, 205, 218), width=2)
    d1.rectangle([160, 770, 380, 800], fill=(150, 160, 175))
    
    # Buttons
    d1.rounded_rectangle([120, 920, 960, 1040], radius=16, fill=(10, 37, 64))
    d1.rectangle([430, 965, 650, 995], fill=(255, 255, 255))
    
    d1.rounded_rectangle([120, 1080, 960, 1200], radius=16, fill=(240, 245, 255), outline=(10, 37, 64), width=2)
    d1.rectangle([380, 1125, 700, 1155], fill=(10, 37, 64))

    # Bottom Android Nav
    d1.rectangle([0, 1840, 1080, 1920], fill=(10, 37, 64))
    
    p1 = os.path.join(desktop, 'Screenshot_1_Login.png')
    s1.save(p1, 'PNG')

    # -------------------------------------------------------------
    # Screenshot 2: Orders & Pickups (1080 x 1920)
    # -------------------------------------------------------------
    s2 = Image.new('RGB', (1080, 1920), (245, 248, 252))
    d2 = ImageDraw.Draw(s2)
    
    d2.rectangle([0, 0, 1080, 70], fill=(10, 37, 64))
    d2.rectangle([0, 70, 1080, 220], fill=(10, 37, 64))
    s2.paste(hl, (60, 95), hl)
    
    d2.rounded_rectangle([760, 105, 980, 175], radius=35, fill=(40, 167, 69))

    d2.rounded_rectangle([60, 260, 1020, 980], radius=24, fill=(255, 255, 255), outline=(215, 222, 232), width=2)
    d2.rounded_rectangle([100, 300, 980, 380], radius=12, fill=(235, 243, 255))
    d2.rectangle([120, 420, 450, 450], fill=(100, 115, 130))
    d2.rectangle([120, 470, 750, 500], fill=(10, 37, 64))
    d2.rectangle([120, 540, 450, 570], fill=(100, 115, 130))
    d2.rectangle([120, 590, 800, 620], fill=(10, 37, 64))
    d2.rectangle([120, 670, 380, 740], fill=(40, 167, 69))
    d2.rounded_rectangle([100, 820, 980, 930], radius=16, fill=(10, 37, 64))

    d2.rounded_rectangle([60, 1020, 1020, 1780], radius=24, fill=(255, 255, 255), outline=(215, 222, 232), width=2)
    d2.rounded_rectangle([100, 1080, 980, 1260], radius=16, fill=(245, 248, 252))
    d2.rounded_rectangle([100, 1300, 980, 1480], radius=16, fill=(245, 248, 252))
    d2.rounded_rectangle([100, 1520, 980, 1700], radius=16, fill=(245, 248, 252))

    d2.rectangle([0, 1840, 1080, 1920], fill=(10, 37, 64))
    
    p2 = os.path.join(desktop, 'Screenshot_2_Orders.png')
    s2.save(p2, 'PNG')

    # -------------------------------------------------------------
    # Screenshot 3: Live Navigation Map (1080 x 1920)
    # -------------------------------------------------------------
    s3 = Image.new('RGB', (1080, 1920), (232, 238, 244))
    d3 = ImageDraw.Draw(s3)
    
    d3.rectangle([0, 0, 1080, 70], fill=(10, 37, 64))
    d3.rectangle([0, 70, 1080, 1350], fill=(232, 238, 244))
    
    # Grid roads
    d3.line([(0, 400), (1080, 400)], fill=(255, 255, 255), width=24)
    d3.line([(0, 900), (1080, 900)], fill=(255, 255, 255), width=24)
    d3.line([(350, 70), (350, 1350)], fill=(255, 255, 255), width=24)
    d3.line([(750, 70), (750, 1350)], fill=(255, 255, 255), width=24)
    
    # Route & Pins
    d3.line([(350, 1100), (350, 900), (750, 900), (750, 400)], fill=(0, 102, 204), width=16)
    d3.ellipse([325, 1075, 375, 1125], fill=(40, 167, 69))
    d3.ellipse([725, 375, 775, 425], fill=(220, 53, 69))

    d3.rounded_rectangle([60, 100, 1020, 240], radius=20, fill=(10, 37, 64))
    d3.rounded_rectangle([60, 1380, 1020, 1800], radius=24, fill=(255, 255, 255), outline=(215, 222, 232), width=2)
    d3.rectangle([110, 1430, 600, 1465], fill=(10, 37, 64))
    d3.rectangle([110, 1485, 800, 1515], fill=(100, 115, 130))
    d3.rounded_rectangle([110, 1560, 520, 1660], radius=16, fill=(240, 245, 255))
    d3.rounded_rectangle([560, 1560, 970, 1660], radius=16, fill=(40, 167, 69))
    d3.rounded_rectangle([110, 1690, 970, 1770], radius=16, fill=(10, 37, 64))

    d3.rectangle([0, 1840, 1080, 1920], fill=(10, 37, 64))
    
    p3 = os.path.join(desktop, 'Screenshot_3_Navigation.png')
    s3.save(p3, 'PNG')

    # -------------------------------------------------------------
    # Screenshot 4: Driver Earnings Dashboard (1080 x 1920)
    # -------------------------------------------------------------
    s4 = Image.new('RGB', (1080, 1920), (245, 248, 252))
    d4 = ImageDraw.Draw(s4)
    
    d4.rectangle([0, 0, 1080, 70], fill=(10, 37, 64))
    d4.rectangle([0, 70, 1080, 220], fill=(10, 37, 64))
    s4.paste(hl, (60, 95), hl)

    d4.rounded_rectangle([60, 260, 1020, 650], radius=24, fill=(10, 37, 64))
    d4.rectangle([110, 310, 450, 340], fill=(180, 195, 215))
    d4.rectangle([110, 370, 600, 450], fill=(255, 255, 255))
    d4.rounded_rectangle([110, 490, 510, 590], radius=16, fill=(25, 55, 88))
    d4.rounded_rectangle([560, 490, 960, 590], radius=16, fill=(25, 55, 88))

    d4.rounded_rectangle([60, 690, 1020, 1250], radius=24, fill=(255, 255, 255), outline=(215, 222, 232), width=2)
    d4.rectangle([110, 730, 450, 765], fill=(10, 37, 64))
    
    bars_x = [140, 260, 380, 500, 620, 740, 860]
    heights = [1050, 980, 920, 1000, 850, 900, 820]
    for bx, hy in zip(bars_x, heights):
        d4.rounded_rectangle([bx, hy, bx+70, 1180], radius=8, fill=(0, 102, 204))

    d4.rounded_rectangle([60, 1290, 1020, 1780], radius=24, fill=(255, 255, 255), outline=(215, 222, 232), width=2)
    d4.rounded_rectangle([100, 1340, 980, 1460], radius=16, fill=(245, 248, 252))
    d4.rounded_rectangle([100, 1490, 980, 1610], radius=16, fill=(245, 248, 252))
    d4.rounded_rectangle([100, 1640, 980, 1740], radius=16, fill=(245, 248, 252))

    d4.rectangle([0, 1840, 1080, 1920], fill=(10, 37, 64))
    
    p4 = os.path.join(desktop, 'Screenshot_4_Earnings.png')
    s4.save(p4, 'PNG')

    print("All 4 screenshots generated successfully on Desktop!")

if __name__ == '__main__':
    generate_screenshots()
