# BossWorld Multi-Boss System - คู่มือการใช้งาน

## สรุปการเปลี่ยนแปลง

ปลั๊กอิน BossWorld ได้รับการอัพเกรดให้รองรับ **หลายบอสพร้อมกัน** โดยแต่ละบอสจะมี:
- ตำแหน่งเกิดแยกจากกัน
- เวลาเกิดแยกจากกัน (configurable)
- การแจ้งเตือนแยกจากกัน
- รางวัลแยกจากกัน

## ไฟล์ที่สำคัญ

**Compiled Plugin:** `target/BossWorld-1.0-SNAPSHOT.jar`

## การตั้งค่า config.yml

```yaml
bosses:
  default:  # Boss ID ตัวแรก
    mythicmob-id: "SkeletalKing"
    display-name: "Skeletal King"
    spawn-interval: 3600  # 1 ชั่วโมง (วินาที)
    announcement-times:
      - 600  # 10 นาที
      - 300  # 5 นาที
      
  boss2:  # Boss ID ที่สอง
    mythicmob-id: "SecondBoss"  # ⚠️ เปลี่ยนเป็น MythicMob ของคุณ
    display-name: "Ancient Dragon"
    spawn-interval: 10800  # 3 ชั่วโมง (วินาที)
    announcement-times:
      - 3600  # 1 ชั่วโมง
      - 1800  # 30 นาที
      - 600   # 10 นาที
```

## คำสั่งใหม่

### สำหรับผู้เล่น
- `/bossworld` - วาร์ปไปบอสเริ่มต้น (default)
- `/bossworld <bossId>` - วาร์ปไปบอสที่ต้องการ (เช่น `/bossworld boss2`)

### สำหรับแอดมิน
- `/bossworld list` - แสดงรายการบอสทั้งหมด
- `/bossworld setspawn <bossId>` - ตั้งตำแหน่งเกิดบอส
- `/bossworld spawn <bossId>` - เสกบอสทันที
- `/bossworld clear [bossId]` - ลบบอส (ถ้าไม่ใส่ bossId จะลบทั้งหมด)
- `/bossworld reward edit <bossId>` - แก้ไขรางวัลของบอส
- `/bossworld reload` - โหลดการตั้งค่าใหม่

## วิธีเพิ่มบอสใหม่

1. เปิด `config.yml`
2. เพิ่มบอสใหม่ใน section `bosses:`:
```yaml
bosses:
  default:
    # ...existing config...
  boss2:
    # ...existing config...
  boss3:  # บอสใหม่
    mythicmob-id: "YourMythicMobID"
    display-name: "Display Name"
    spawn-interval: 7200  # 2 ชั่วโมง
    announcement-times:
      - 1200
      - 600
```
3. รันคำสั่ง `/bossworld reload`
4. ตั้งตำแหน่งเกิดด้วย `/bossworld setspawn boss3`
5. ตั้งรางวัลด้วย `/bossworld reward edit boss3`

## การจัดการรางวัล

รางวัลแต่ละบอสถูกจัดเก็บแยกกันใน `data.yml`:
```yaml
rewards:
  default:
    0:
      item: {...}
      chance: 50.0
  boss2:
    0:
      item: {...}
      chance: 30.0
```

## การแจ้งเตือน Discord

Discord webhook จะแจ้งเตือนทุกบอสแยกกัน โดยใช้ `%boss%` และ `%boss_id%` placeholders

## ระบบ Anti-Cheat

ระบบ anti-cheat จะทำงานรอบ ๆ **ทุกบอสที่กำลัง active** โดยอัตโนมัติ

## ข้อมูลเพิ่มเติม

- **Build Status:** ✅ SUCCESS
- **PaperMC Version:** 1.21
- **Java Version:** 21
- **Dependencies:** MythicMobs 5.6.1, CMI (optional)

## การติดตั้ง

1. คัดลอก `target/BossWorld-1.0-SNAPSHOT.jar` ไปใน `plugins/`
2. รีสตาร์ทเซิร์ฟเวอร์
3. แก้ไข `plugins/BossWorld/config.yml`
4. รัน `/bossworld reload`
5. ตั้งตำแหน่งเกิดบอสด้วย `/bossworld setspawn <bossId>`

## หมายเหตุสำคัญ

⚠️ **อย่าลืม:** เปลี่ยน `mythicmob-id: "SecondBoss"` ใน config.yml เป็น MythicMob ID ที่คุณต้องการใช้จริง!
