import asyncio
import aiohttp
import time
import random
import tkinter as tk
from tkinter import ttk, filedialog, scrolledtext, messagebox
import threading
import queue
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

API_URL = 'https://cybershield-backend-renderserver.onrender.com/api/analyze'

class LoadTestGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Giám sát kiểm tra tải CyberShield")
        self.root.geometry("1200x800")
        
        self.lines = []
        self.is_running = False
        self.results_queue = queue.Queue()
        self.test_results = []
        self.current_concurrent = 1
        
        self.setup_ui()
        self.setup_chart()
        
    def setup_ui(self):
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        self.root.columnconfigure(0, weight=1)
        self.root.rowconfigure(0, weight=1)
        main_frame.columnconfigure(1, weight=1)
        main_frame.rowconfigure(2, weight=1)
        
        # Chọn tệp
        file_frame = ttk.LabelFrame(main_frame, text="Cấu hình tệp", padding="5")
        file_frame.grid(row=0, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
        
        self.file_path = tk.StringVar(value="D:/DuAn/CyberShield_Ver_1_0/test_server/truyenkieu.txt")
        ttk.Label(file_frame, text="Tệp văn bản:").grid(row=0, column=0, sticky=tk.W)
        ttk.Entry(file_frame, textvariable=self.file_path, width=60).grid(row=0, column=1, padx=(5, 5))
        ttk.Button(file_frame, text="Duyệt", command=self.browse_file).grid(row=0, column=2)
        ttk.Button(file_frame, text="Tải", command=self.load_file).grid(row=0, column=3, padx=(5, 0))
        
        # Bảng điều khiển
        control_frame = ttk.LabelFrame(main_frame, text="Điều khiển kiểm tra", padding="5")
        control_frame.grid(row=1, column=0, sticky=(tk.W, tk.E), pady=(0, 10))
        
        self.start_btn = ttk.Button(control_frame, text="Bắt đầu kiểm tra", command=self.start_test)
        self.start_btn.grid(row=0, column=0, padx=(0, 10))
        
        self.stop_btn = ttk.Button(control_frame, text="Dừng kiểm tra", command=self.stop_test, state="disabled")
        self.stop_btn.grid(row=0, column=1, padx=(0, 10))
        
        ttk.Button(control_frame, text="Xóa kết quả", command=self.clear_results).grid(row=0, column=2)
        
        # Chọn loại kiểm tra
        test_type_frame = ttk.LabelFrame(main_frame, text="Loại kiểm tra", padding="5")
        test_type_frame.grid(row=1, column=1, sticky=(tk.W, tk.E), padx=(10, 0), pady=(0, 10))
        
        self.test_type = tk.StringVar(value="concurrent")
        ttk.Radiobutton(test_type_frame, text="Đồng thời", variable=self.test_type, value="concurrent").grid(row=0, column=0, padx=5)
        ttk.Radiobutton(test_type_frame, text="Tuần tự", variable=self.test_type, value="sequential").grid(row=0, column=1, padx=5)
        
        # Chỉ báo trạng thái
        status_frame = ttk.LabelFrame(main_frame, text="Trạng thái", padding="5")
        status_frame.grid(row=1, column=2, sticky=(tk.W, tk.E), padx=(10, 0), pady=(0, 10))
        
        ttk.Label(status_frame, text="Số dòng đã tải:").grid(row=0, column=0, sticky=tk.W)
        self.lines_count_label = ttk.Label(status_frame, text="0")
        self.lines_count_label.grid(row=0, column=1, sticky=tk.W, padx=(10, 20))
        
        ttk.Label(status_frame, text="Số lượng đồng thời hiện tại:").grid(row=0, column=2, sticky=tk.W)
        self.concurrent_label = ttk.Label(status_frame, text="0", font=("TkDefaultFont", 10, "bold"))
        self.concurrent_label.grid(row=0, column=3, sticky=tk.W, padx=(10, 0))
        
        # Khu vực kết quả với các tab
        notebook = ttk.Notebook(main_frame)
        notebook.grid(row=2, column=0, columnspan=3, sticky=(tk.W, tk.E, tk.N, tk.S), pady=(0, 10))
        
        # Tab nhật ký
        log_frame = ttk.Frame(notebook)
        notebook.add(log_frame, text="Nhật ký thời gian thực")
        
        self.log_text = scrolledtext.ScrolledText(log_frame, height=15, width=80)
        self.log_text.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        # Tab biểu đồ
        chart_frame = ttk.Frame(notebook)
        notebook.add(chart_frame, text="Biểu đồ hiệu suất")
        self.chart_frame = chart_frame
        
        # Tab tóm tắt
        summary_frame = ttk.Frame(notebook)
        notebook.add(summary_frame, text="Tóm tắt")
        
        self.summary_tree = ttk.Treeview(summary_frame, columns=("concurrent", "success", "failed", "avg_time", "max_time"), show="headings", height=10)
        self.summary_tree.heading("concurrent", text="Đồng thời")
        self.summary_tree.heading("success", text="Thành công")
        self.summary_tree.heading("failed", text="Thất bại")
        self.summary_tree.heading("avg_time", text="Thời gian trung bình (s)")
        self.summary_tree.heading("max_time", text="Thời gian tối đa (s)")
        self.summary_tree.column("concurrent", width=80)
        self.summary_tree.column("success", width=80)
        self.summary_tree.column("failed", width=80)
        self.summary_tree.column("avg_time", width=100)
        self.summary_tree.column("max_time", width=100)
        self.summary_tree.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        # Thanh trạng thái
        self.status_var = tk.StringVar(value="Sẵn sàng")
        status_bar = ttk.Label(main_frame, textvariable=self.status_var, relief=tk.SUNKEN)
        status_bar.grid(row=3, column=0, columnspan=3, sticky=(tk.W, tk.E))
        
    def setup_chart(self):
        self.fig, (self.ax1, self.ax2) = plt.subplots(2, 1, figsize=(10, 6))
        self.fig.tight_layout()
        self.ax1.set_title("Tỷ lệ thành công so với yêu cầu đồng thời")
        self.ax1.set_xlabel("Yêu cầu đồng thời")
        self.ax1.set_ylabel("Tỷ lệ thành công (%)")
        self.ax1.grid(True)
        self.ax2.set_title("Thời gian phản hồi trung bình so với yêu cầu đồng thời")
        self.ax2.set_xlabel("Yêu cầu đồng thời")
        self.ax2.set_ylabel("Thời gian phản hồi (s)")
        self.ax2.grid(True)
        self.canvas = FigureCanvasTkAgg(self.fig, master=self.chart_frame)
        self.canvas.draw()
        self.canvas.get_tk_widget().pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
    def browse_file(self):
        filename = filedialog.askopenfilename(
            title="Chọn tệp văn bản",
            filetypes=[("Tệp văn bản", "*.txt"), ("Tất cả các tệp", "*.*")]
        )
        if filename:
            self.file_path.set(filename)
            
    def load_file(self):
        try:
            with open(self.file_path.get(), 'r', encoding='utf-8') as f:
                lines = f.readlines()
            self.lines = [line.strip() for line in lines if len(line.strip()) > 10]
            self.lines_count_label.config(text=str(len(self.lines)))
            self.log_message(f"✅ Đã tải {len(self.lines)} dòng hợp lệ từ tệp")
            if len(self.lines) < 20:
                messagebox.showwarning("Cảnh báo", "Tệp có ít hơn 20 dòng, kiểm tra có thể bị lặp dữ liệu.")
        except Exception as e:
            messagebox.showerror("Lỗi", f"Không thể tải tệp: {str(e)}")
            self.log_message(f"❌ Không thể tải tệp: {str(e)}")
    
    def log_message(self, message):
        timestamp = time.strftime("%H:%M:%S")
        self.log_text.insert(tk.END, f"[{timestamp}] {message}\n")
        self.log_text.see(tk.END)
        
    def start_test(self):
        if not self.lines:
            messagebox.showwarning("Cảnh báo", "Vui lòng tải tệp văn bản trước")
            return
        self.is_running = True
        self.start_btn.config(state="disabled")
        self.stop_btn.config(state="normal")
        self.current_concurrent = 1
        self.test_results = []
        self.log_message("🚀 Bắt đầu kiểm tra tải...")
        self.status_var.set("Đang kiểm tra...")
        thread = threading.Thread(target=self.run_test_thread)
        thread.daemon = True
        thread.start()
        self.process_queue()
        
    def stop_test(self):
        self.is_running = False
        self.start_btn.config(state="normal")
        self.stop_btn.config(state="disabled")
        self.status_var.set("Kiểm tra đã dừng")
        self.log_message("⏹️ Kiểm tra đã dừng bởi người dùng")
        
    def clear_results(self):
        self.log_text.delete(1.0, tk.END)
        self.test_results = []
        for item in self.summary_tree.get_children():
            self.summary_tree.delete(item)
        self.ax1.clear()
        self.ax2.clear()
        self.setup_chart()
        self.canvas.draw()
        
    def run_test_thread(self):
        asyncio.run(self.run_test())
        
    async def run_test(self):
        while self.is_running:
            self.concurrent_label.config(text=str(self.current_concurrent))
            self.results_queue.put(("status", f"Đang kiểm tra với {self.current_concurrent} yêu cầu đồng thời"))
            
            if self.test_type.get() == "concurrent":
                success = await self.test_load(self.current_concurrent)
            else:
                success = await self.test_sequential(self.current_concurrent)
            
            if not success and self.is_running:
                self.results_queue.put(("complete", f"Máy chủ bắt đầu thất bại ở {self.current_concurrent} yêu cầu đồng thời"))
                break
            if self.is_running:
                self.current_concurrent += 1
                await asyncio.sleep(1)
        self.results_queue.put(("finished", "Kiểm tra đã hoàn thành"))
        
    async def test_load(self, concurrent_requests):
        start_time = time.time()
        async with aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(total=60)) as session:
            tasks = [self.send_request(session, i+1) for i in range(concurrent_requests)]
            results = await asyncio.gather(*tasks)
        success_results = [r for r in results if r[1]]
        failed_results = [r for r in results if not r[1]]
        success_count = len(success_results)
        failed_count = len(failed_results)
        success_rate = (success_count / concurrent_requests) * 100
        response_times = [r[2] for r in success_results if isinstance(r[2], (int, float))]
        avg_time = sum(response_times) / len(response_times) if response_times else 0
        max_time = max(response_times) if response_times else 0
        test_result = {
            'concurrent': concurrent_requests,
            'success': success_count,
            'failed': failed_count,
            'success_rate': success_rate,
            'avg_time': avg_time,
            'max_time': max_time,
            'details': results
        }
        self.results_queue.put(("result", test_result))
        return success_count == concurrent_requests
    
    async def test_sequential(self, requests):
        async with aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(total=60)) as session:
            for i in range(requests):
                idx, ok, info = await self.send_request(session, i+1)
                if ok:
                    self.results_queue.put(("status", f"Yêu cầu #{idx}: ✅ thành công trong {info:.2f}s"))
                else:
                    self.results_queue.put(("status", f"Yêu cầu #{idx}: ❌ thất bại - {info}"))
                    return False
        return True

    async def send_request(self, session, idx):
        text_to_send = self.generate_random_text() + f" (yêu cầu #{idx} - {time.time_ns()})"
        data = {'text': text_to_send}
        start = time.perf_counter()
        try:
            async with session.post(API_URL, json=data) as resp:
                if resp.status != 200:
                    return idx, False, f"HTTP {resp.status}"
                await resp.json()
                end = time.perf_counter()
                return idx, True, end - start
        except Exception as e:
            return idx, False, str(e)

    def generate_random_text(self, count=20):
        chosen = random.sample(self.lines, min(count, len(self.lines)))
        return ' '.join(chosen)

    def process_queue(self):
        try:
            while True:
                msg_type, data = self.results_queue.get_nowait()
                if msg_type == "status":
                    self.log_message(f"📊 {data}")
                elif msg_type == "result":
                    self.process_test_result(data)
                elif msg_type == "complete":
                    self.log_message(f"🔴 {data}")
                    self.status_var.set("Kiểm tra đã hoàn thành - Đã đạt giới hạn máy chủ")
                elif msg_type == "finished":
                    self.is_running = False
                    self.start_btn.config(state="normal")
                    self.stop_btn.config(state="disabled")
                    if self.status_var.get() == "Đang kiểm tra...":
                        self.status_var.set("Sẵn sàng")
        except queue.Empty:
            pass
        if self.is_running or not self.results_queue.empty():
            self.root.after(100, self.process_queue)
            
    def process_test_result(self, result):
        self.test_results.append(result)
        self.log_message(f"✅ {result['concurrent']} đồng thời: {result['success']}/{result['concurrent']} thành công, "
                         f"trung bình: {result['avg_time']:.2f}s, tối đa: {result['max_time']:.2f}s")
        for idx, success, info in result['details']:
            if success:
                self.log_message(f"   Yêu cầu #{idx}: ✅ {info:.2f}s")
            else:
                self.log_message(f"   Yêu cầu #{idx}: ❌ {info}")
        self.summary_tree.insert("", "end", values=(
            result['concurrent'],
            result['success'],
            result['failed'],
            f"{result['avg_time']:.2f}",
            f"{result['max_time']:.2f}"
        ))
        self.update_charts()
        
    def update_charts(self):
        if not self.test_results:
            return
        concurrent_vals = [r['concurrent'] for r in self.test_results]
        success_rates = [r['success_rate'] for r in self.test_results]
        avg_times = [r['avg_time'] for r in self.test_results]
        self.ax1.clear()
        self.ax1.plot(concurrent_vals, success_rates, 'bo-', linewidth=2, markersize=6)
        self.ax1.set_title("Tỷ lệ thành công so với yêu cầu đồng thời")
        self.ax1.set_xlabel("Yêu cầu đồng thời")
        self.ax1.set_ylabel("Tỷ lệ thành công (%)")
        self.ax1.grid(True)
        self.ax1.set_ylim(0, 105)
        self.ax2.clear()
        self.ax2.plot(concurrent_vals, avg_times, 'ro-', linewidth=2, markersize=6)
        self.ax2.set_title("Thời gian phản hồi trung bình so với yêu cầu đồng thời")
        self.ax2.set_xlabel("Yêu cầu đồng thời")
        self.ax2.set_ylabel("Thời gian phản hồi (s)")
        self.ax2.grid(True)
        self.fig.tight_layout()
        self.canvas.draw()

def main():
    root = tk.Tk()
    app = LoadTestGUI(root)
    try:
        app.load_file()
    except:
        pass
    root.mainloop()

if __name__ == '__main__':
    main()
